package com.example.autoconnect.data.service

import android.content.Context
import android.util.Log
import com.example.autoconnect.R
import com.example.autoconnect.data.SampleData
import com.example.autoconnect.data.local.AppDatabase
import com.example.autoconnect.data.local.ServiceProviderEntity
import com.example.autoconnect.data.model.ServiceCategory
import com.example.autoconnect.data.model.ServiceProvider
import com.example.autoconnect.data.sync.awaitTask
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

/**
 * Service Layer dedicated to querying and searching local mechanics in Mali
 * directly against Cloud Firestore, with graceful offline fallback to Room database.
 */
class FirestoreMechanicSearchService(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val serviceDao = db.serviceProviderDao()

    private fun getFirestore(): FirebaseFirestore {
        val databaseId = context.getString(R.string.firestore_database_id)
        return FirebaseFirestore.getInstance(databaseId)
    }

    /**
     * Search mechanics with real-time Firestore updates.
     * Emits search results matching query, city, open status, and minimum rating.
     */
    fun searchMechanicsRealtime(
        query: String = "",
        city: String? = null,
        onlyOpen: Boolean = false,
        minRating: Double = 0.0
    ): Flow<Result<List<ServiceProvider>>> = callbackFlow {
        try {
            val firestore = getFirestore()
            var baseQuery: Query = firestore.collection("services")
                .whereEqualTo("category", ServiceCategory.MECANICIEN.name)

            if (!city.isNullOrBlank() && city != "Toutes") {
                baseQuery = baseQuery.whereEqualTo("city", city)
            }

            val registration = baseQuery.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Firestore query error, falling back to local Room database: ${error.message}")
                    // Fallback to local Room database
                    trySend(Result.success(searchLocalRoomFallback(query, city, onlyOpen, minRating)))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val mechanics = mutableListOf<ServiceProvider>()
                    for (doc in snapshot.documents) {
                        val provider = parseDocumentToServiceProvider(doc)
                        if (provider != null) {
                            mechanics.add(provider)
                        }
                    }

                    // In-memory text matching and filtering
                    val filtered = filterMechanics(mechanics, query, city, onlyOpen, minRating)

                    // If Firestore collection was empty, fallback to local Room data so users always see Mali mechanics
                    if (filtered.isEmpty() && snapshot.isEmpty) {
                        val fallback = searchLocalRoomFallback(query, city, onlyOpen, minRating)
                        trySend(Result.success(fallback))
                    } else {
                        trySend(Result.success(filtered))
                    }
                }
            }

            awaitClose {
                registration.remove()
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            // Normal coroutine cancellation when leaving composition or updating query
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error initiating Firestore real-time search: ${e.message}", e)
            val fallback = searchLocalRoomFallback(query, city, onlyOpen, minRating)
            trySend(Result.success(fallback))
            close(e)
        }
    }

    /**
     * One-shot search for mechanics in Mali from Firestore with suspend execution.
     */
    suspend fun searchMechanicsOnce(
        query: String = "",
        city: String? = null,
        onlyOpen: Boolean = false,
        minRating: Double = 0.0
    ): Result<List<ServiceProvider>> {
        return try {
            val firestore = getFirestore()
            var baseQuery: Query = firestore.collection("services")
                .whereEqualTo("category", ServiceCategory.MECANICIEN.name)

            if (!city.isNullOrBlank() && city != "Toutes") {
                baseQuery = baseQuery.whereEqualTo("city", city)
            }

            val snapshot = baseQuery.get().awaitTask()
            val list = mutableListOf<ServiceProvider>()
            for (doc in snapshot.documents) {
                val provider = parseDocumentToServiceProvider(doc)
                if (provider != null) {
                    list.add(provider)
                }
            }

            val filtered = filterMechanics(list, query, city, onlyOpen, minRating)
            if (filtered.isEmpty() && snapshot.isEmpty) {
                Result.success(searchLocalRoomFallback(query, city, onlyOpen, minRating))
            } else {
                Result.success(filtered)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firestore one-shot search failed: ${e.message}, falling back to Room.")
            Result.success(searchLocalRoomFallback(query, city, onlyOpen, minRating))
        }
    }

    /**
     * Fetch a specific mechanic by ID from Firestore, with Room fallback.
     */
    suspend fun getMechanicById(id: String): ServiceProvider? {
        return try {
            val doc = getFirestore().collection("services").document(id).get().awaitTask()
            if (doc.exists()) {
                parseDocumentToServiceProvider(doc)
            } else {
                serviceDao.getServiceById(id)?.toDomainModel()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error fetching mechanic $id from Firestore: ${e.message}")
            serviceDao.getServiceById(id)?.toDomainModel()
        }
    }

    /**
     * List of major cities and automotive hubs in Mali.
     */
    fun getMaliCities(): List<String> {
        return listOf(
            "Toutes",
            "Bamako",
            "Koulikoro",
            "Ségou",
            "Sikasso",
            "Mopti",
            "Kayes",
            "Gao",
            "Kidal",
            "Tombouctou"
        )
    }

    /**
     * Common automotive specialties in Mali workshops.
     */
    fun getSpecialties(): List<String> {
        return listOf(
            "Tous",
            "Diagnostic OBD",
            "Vidange Express",
            "Freinage & ABS",
            "Climatisation",
            "Injection Diesel",
            "Électricité Auto",
            "Dépannage SOS"
        )
    }

    private fun filterMechanics(
        list: List<ServiceProvider>,
        query: String,
        city: String?,
        onlyOpen: Boolean,
        minRating: Double
    ): List<ServiceProvider> {
        val cleanQuery = query.trim().lowercase()
        return list.filter { m ->
            val matchesQuery = cleanQuery.isEmpty() ||
                    m.name.lowercase().contains(cleanQuery) ||
                    m.description.lowercase().contains(cleanQuery) ||
                    m.city.lowercase().contains(cleanQuery) ||
                    (m.servicesOffered?.lowercase()?.contains(cleanQuery) == true)

            val matchesCity = city.isNullOrBlank() || city == "Toutes" || m.city.equals(city, ignoreCase = true)
            val matchesOpen = !onlyOpen || m.isOpen
            val matchesRating = m.rating >= minRating

            matchesQuery && matchesCity && matchesOpen && matchesRating
        }.sortedByDescending { it.rating }
    }

    private fun searchLocalRoomFallback(
        query: String,
        city: String?,
        onlyOpen: Boolean,
        minRating: Double
    ): List<ServiceProvider> {
        val providers: List<ServiceProvider> = try {
            kotlinx.coroutines.runBlocking {
                val entities = serviceDao.getMechanics().firstOrNull()
                if (!entities.isNullOrEmpty()) {
                    entities.map { it.toDomainModel() }
                } else {
                    SampleData.sampleProviders.filter { it.category == ServiceCategory.MECANICIEN }
                }
            }
        } catch (e: Exception) {
            SampleData.sampleProviders.filter { it.category == ServiceCategory.MECANICIEN }
        }

        return filterMechanics(providers, query, city, onlyOpen, minRating)
    }

    private fun parseDocumentToServiceProvider(doc: DocumentSnapshot): ServiceProvider? {
        val data = doc.data ?: return null
        return try {
            val id = (data["id"] as? String)?.takeIf { it.isNotBlank() } ?: doc.id
            val name = (data["name"] as? String)?.takeIf { it.isNotBlank() } ?: return null
            val categoryStr = data["category"] as? String ?: ServiceCategory.MECANICIEN.name
            val desc = data["description"] as? String ?: ""
            val city = data["city"] as? String ?: "Bamako"
            val phone = data["phone"] as? String ?: ""
            val rating = (data["rating"] as? Number)?.toDouble() ?: 4.8
            val lat = (data["latitude"] as? Number)?.toDouble() ?: 12.6392
            val lng = (data["longitude"] as? Number)?.toDouble() ?: -8.0029
            val isOpen = data["isOpen"] as? Boolean ?: true
            val hours = data["hours"] as? String ?: "08:00 - 18:30"
            val servicesOffered = data["servicesOffered"] as? String

            ServiceProvider(
                id = id,
                name = name,
                category = ServiceCategory.fromString(categoryStr),
                description = desc,
                city = city,
                phone = phone,
                rating = rating,
                latitude = lat,
                longitude = lng,
                isOpen = isOpen,
                isFavorite = false,
                isMine = false,
                hours = hours,
                servicesOffered = servicesOffered
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse mechanic document ${doc.id}", e)
            null
        }
    }

    companion object {
        private const val TAG = "MechanicSearchService"
    }
}
