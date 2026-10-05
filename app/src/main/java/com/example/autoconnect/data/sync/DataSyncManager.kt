package com.example.autoconnect.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.example.autoconnect.R
import com.example.autoconnect.data.SampleData
import com.example.autoconnect.data.local.AppDatabase
import com.example.autoconnect.data.local.ServiceProviderEntity
import com.example.autoconnect.data.model.ServiceCategory
import com.example.autoconnect.data.model.ServiceProvider
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class SyncSummary(
    val totalItems: Int,
    val mechanicsCount: Int,
    val partsShopsCount: Int,
    val tiresCount: Int,
    val timestamp: Long,
    val isFromCache: Boolean = false,
    val message: String = ""
)

sealed class SyncStatus {
    data class Idle(
        val lastSyncTimestamp: Long = 0L,
        val totalItems: Int = 0
    ) : SyncStatus()

    data class Syncing(
        val message: String = "Synchronisation en cours...",
        val progress: Float = 0f
    ) : SyncStatus()

    data class Success(
        val summary: SyncSummary
    ) : SyncStatus()

    data class Error(
        val message: String,
        val cachedItems: Int = 0,
        val lastSyncTimestamp: Long = 0L
    ) : SyncStatus()
}

class DataSyncManager(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val serviceDao = db.serviceProviderDao()

    private val prefs = context.getSharedPreferences("autoconnect_data_sync", Context.MODE_PRIVATE)

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle(
        lastSyncTimestamp = prefs.getLong(KEY_LAST_SYNC_TIME, 0L),
        totalItems = prefs.getInt(KEY_LAST_SYNC_COUNT, 0)
    ))
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _isOnline = MutableStateFlow(checkNetworkDirectly())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private var realtimeListener: ListenerRegistration? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    init {
        registerNetworkObserver()
    }

    private fun checkNetworkDirectly(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun registerNetworkObserver() {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _isOnline.value = true
                }

                override fun onLost(network: Network) {
                    _isOnline.value = false
                }
            }
            networkCallback = callback
            cm.registerNetworkCallback(request, callback)
        } catch (e: Exception) {
            Log.w(TAG, "Could not register network callback: ${e.message}")
        }
    }

    private fun getFirestore(): FirebaseFirestore {
        val databaseId = context.getString(R.string.firestore_database_id)
        return FirebaseFirestore.getInstance(databaseId)
    }

    /**
     * Suspend function to fetch mechanic and parts shop data from Firestore,
     * transform and upsert directly into the local Room database for offline access.
     */
    suspend fun syncFromFirestore(forceRefresh: Boolean = false): Result<SyncSummary> = withContext(Dispatchers.IO) {
        val online = checkNetworkDirectly()
        _isOnline.value = online

        if (!online) {
            // Offline fallback: load cached data from Room
            val cachedTotal = serviceDao.getCount()
            val cachedMechanics = serviceDao.getCountByCategory(ServiceCategory.MECANICIEN.name)
            val cachedParts = serviceDao.getCountByCategory(ServiceCategory.PIECES.name)
            val cachedTires = serviceDao.getCountByCategory(ServiceCategory.PNEUMATIQUE.name)
            val lastSyncTime = prefs.getLong(KEY_LAST_SYNC_TIME, 0L)

            val summary = SyncSummary(
                totalItems = cachedTotal,
                mechanicsCount = cachedMechanics,
                partsShopsCount = cachedParts,
                tiresCount = cachedTires,
                timestamp = lastSyncTime,
                isFromCache = true,
                message = "Mode hors-ligne : $cachedTotal prestataires disponibles localement (dont $cachedMechanics mécaniciens et $cachedParts boutiques de pièces)."
            )

            _syncStatus.value = SyncStatus.Error(
                message = "Réseau indisponible. Utilisation de la base locale Room.",
                cachedItems = cachedTotal,
                lastSyncTimestamp = lastSyncTime
            )

            return@withContext Result.success(summary)
        }

        try {
            _syncStatus.value = SyncStatus.Syncing("Connexion au Cloud Firestore...", 0.2f)
            val firestore = getFirestore()

            // Fetch all documents from Firestore "services" collection
            val collectionRef = firestore.collection("services")
            val snapshot: QuerySnapshot = collectionRef.get().awaitTask()

            _syncStatus.value = SyncStatus.Syncing("Traitement des mécaniciens et boutiques de pièces...", 0.5f)

            val existingLocalServices = serviceDao.getAllServices().first()
            val localFavoritesMap = existingLocalServices.associate { it.id to it.isFavorite }
            val localMineMap = existingLocalServices.associate { it.id to it.isMine }

            val entitiesToUpsert = mutableListOf<ServiceProviderEntity>()

            if (snapshot.isEmpty) {
                // If Firestore is empty, seed it with default mechanics and parts shops so cloud data exists
                Log.d(TAG, "Firestore services collection is empty, seeding initial services to Firestore...")
                _syncStatus.value = SyncStatus.Syncing("Initialisation du catalogue Cloud...", 0.6f)
                
                for (sample in SampleData.sampleProviders) {
                    val payload = hashMapOf<String, Any>(
                        "id" to sample.id,
                        "name" to sample.name,
                        "category" to sample.category.name,
                        "description" to sample.description,
                        "city" to sample.city,
                        "phone" to sample.phone,
                        "rating" to sample.rating,
                        "latitude" to sample.latitude,
                        "longitude" to sample.longitude,
                        "isOpen" to sample.isOpen,
                        "hours" to (sample.hours ?: ""),
                        "servicesOffered" to (sample.servicesOffered ?: "")
                    )
                    Firebase.auth.currentUser?.uid?.let { uid ->
                        payload["ownerId"] = uid
                    }
                    try {
                        collectionRef.document(sample.id).set(payload).awaitTask()
                    } catch (e: Exception) {
                        Log.w(TAG, "Error seeding provider ${sample.id} to Firestore: ${e.message}")
                    }
                    entitiesToUpsert.add(ServiceProviderEntity.fromDomainModel(sample))
                }
            } else {
                for (doc in snapshot.documents) {
                    val entity = parseDocumentToEntity(doc, localFavoritesMap, localMineMap)
                    if (entity != null) {
                        entitiesToUpsert.add(entity)
                    }
                }
            }

            _syncStatus.value = SyncStatus.Syncing("Enregistrement dans la base locale SQLite (Room)...", 0.8f)

            if (entitiesToUpsert.isNotEmpty()) {
                serviceDao.upsertAll(entitiesToUpsert)
            }

            // Calculate updated counts from local database
            val total = serviceDao.getCount()
            val mechanicsCount = serviceDao.getCountByCategory(ServiceCategory.MECANICIEN.name)
            val partsShopsCount = serviceDao.getCountByCategory(ServiceCategory.PIECES.name)
            val tiresCount = serviceDao.getCountByCategory(ServiceCategory.PNEUMATIQUE.name)
            val now = System.currentTimeMillis()

            // Save last sync info in preferences
            prefs.edit()
                .putLong(KEY_LAST_SYNC_TIME, now)
                .putInt(KEY_LAST_SYNC_COUNT, total)
                .putInt(KEY_LAST_MECHANICS_COUNT, mechanicsCount)
                .putInt(KEY_LAST_PARTS_COUNT, partsShopsCount)
                .apply()

            val summary = SyncSummary(
                totalItems = total,
                mechanicsCount = mechanicsCount,
                partsShopsCount = partsShopsCount,
                tiresCount = tiresCount,
                timestamp = now,
                isFromCache = false,
                message = "Synchronisation réussie : $total prestataires ($mechanicsCount mécaniciens, $partsShopsCount pièces auto) enregistrés pour accès hors-ligne."
            )

            _syncStatus.value = SyncStatus.Success(summary)
            Log.i(TAG, "Data synchronization completed successfully: $summary")
            Result.success(summary)

        } catch (e: Exception) {
            Log.e(TAG, "Error synchronizing from Firestore: ${e.message}", e)
            val cachedTotal = serviceDao.getCount()
            val lastSyncTime = prefs.getLong(KEY_LAST_SYNC_TIME, 0L)
            val errorMsg = "Erreur de synchronisation Cloud (${e.localizedMessage ?: "Erreur réseau"}). $cachedTotal prestataires conservés hors-ligne."
            _syncStatus.value = SyncStatus.Error(
                message = errorMsg,
                cachedItems = cachedTotal,
                lastSyncTimestamp = lastSyncTime
            )
            Result.failure(e)
        }
    }

    /**
     * Real-time continuous sync: keeps the local Room database updated
     * whenever changes occur in Firestore.
     */
    fun startRealtimeSync(scope: CoroutineScope) {
        try {
            val firestore = getFirestore()
            realtimeListener?.remove()

            realtimeListener = firestore.collection("services")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Firestore snapshot listener error: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        scope.launch(Dispatchers.IO) {
                            val existing = serviceDao.getAllServices().first()
                            val favMap = existing.associate { it.id to it.isFavorite }
                            val mineMap = existing.associate { it.id to it.isMine }

                            val updatedEntities = mutableListOf<ServiceProviderEntity>()
                            for (doc in snapshot.documents) {
                                val entity = parseDocumentToEntity(doc, favMap, mineMap)
                                if (entity != null) {
                                    updatedEntities.add(entity)
                                }
                            }

                            if (updatedEntities.isNotEmpty()) {
                                serviceDao.upsertAll(updatedEntities)
                                val count = serviceDao.getCount()
                                prefs.edit()
                                    .putLong(KEY_LAST_SYNC_TIME, System.currentTimeMillis())
                                    .putInt(KEY_LAST_SYNC_COUNT, count)
                                    .apply()
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting realtime sync", e)
        }
    }

    fun stopRealtimeSync() {
        realtimeListener?.remove()
        realtimeListener = null
    }

    private fun parseDocumentToEntity(
        doc: DocumentSnapshot,
        localFavoritesMap: Map<String, Boolean>,
        localMineMap: Map<String, Boolean>
    ): ServiceProviderEntity? {
        val data = doc.data ?: return null
        return try {
            val id = (data["id"] as? String)?.takeIf { it.isNotBlank() } ?: doc.id
            val name = (data["name"] as? String)?.takeIf { it.isNotBlank() } ?: return null
            val categoryStr = data["category"] as? String ?: "AUTRE"
            val category = ServiceCategory.fromString(categoryStr)
            val desc = data["description"] as? String ?: ""
            val city = data["city"] as? String ?: "Bamako"
            val phone = data["phone"] as? String ?: ""
            val rating = (data["rating"] as? Number)?.toDouble() ?: 5.0
            val lat = (data["latitude"] as? Number)?.toDouble() ?: 12.6392
            val lng = (data["longitude"] as? Number)?.toDouble() ?: -8.0029
            val isOpen = data["isOpen"] as? Boolean ?: true
            val hours = data["hours"] as? String
            val servicesOffered = data["servicesOffered"] as? String

            // Preserve local user interactions
            val isFavorite = localFavoritesMap[id] ?: false
            val isMine = localMineMap[id] ?: false

            ServiceProviderEntity(
                id = id,
                name = name,
                category = category.name,
                description = desc,
                city = city,
                phone = phone,
                rating = rating,
                latitude = lat,
                longitude = lng,
                isOpen = isOpen,
                isFavorite = isFavorite,
                isMine = isMine,
                hours = hours,
                servicesOffered = servicesOffered
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse document ${doc.id}", e)
            null
        }
    }

    fun getLastSyncFormatted(): String {
        val time = prefs.getLong(KEY_LAST_SYNC_TIME, 0L)
        if (time == 0L) return "Jamais synchronisé"
        val diff = System.currentTimeMillis() - time
        return when {
            diff < 60_000 -> "À l'instant"
            diff < 3600_000 -> "Il y a ${diff / 60_000} min"
            diff < 86400_000 -> "Il y a ${diff / 3600_000} h"
            else -> {
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH)
                sdf.format(Date(time))
            }
        }
    }

    companion object {
        private const val TAG = "DataSyncManager"
        private const val KEY_LAST_SYNC_TIME = "key_last_sync_time"
        private const val KEY_LAST_SYNC_COUNT = "key_last_sync_count"
        private const val KEY_LAST_MECHANICS_COUNT = "key_last_mechanics_count"
        private const val KEY_LAST_PARTS_COUNT = "key_last_parts_count"

        @Volatile
        private var INSTANCE: DataSyncManager? = null

        fun getInstance(context: Context): DataSyncManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DataSyncManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

/**
 * Task extension helper for coroutine-friendly await without external dependencies.
 */
suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result ->
        if (continuation.isActive) {
            continuation.resume(result)
        }
    }
    addOnFailureListener { exception ->
        if (continuation.isActive) {
            continuation.resumeWithException(exception)
        }
    }
    addOnCanceledListener {
        if (continuation.isActive) {
            continuation.cancel()
        }
    }
}
