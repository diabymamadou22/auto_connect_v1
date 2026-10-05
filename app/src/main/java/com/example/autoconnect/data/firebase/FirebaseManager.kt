package com.example.autoconnect.data.firebase

import android.content.Context
import android.util.Log
import com.example.autoconnect.R
import com.example.autoconnect.data.local.BookingEntity
import com.example.autoconnect.data.model.Review
import com.example.autoconnect.data.model.ServiceCategory
import com.example.autoconnect.data.model.ServiceProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object FirebaseManager {
    private const val TAG = "FirebaseManager"
    private var serviceListenerRegistration: ListenerRegistration? = null

    fun getFirestore(context: Context): FirebaseFirestore {
        val databaseId = context.getString(R.string.firestore_database_id)
        return FirebaseFirestore.getInstance(databaseId)
    }

    fun syncFromFirestore(
        context: Context,
        scope: CoroutineScope,
        onServiceReceived: suspend (ServiceProvider) -> Unit
    ) {
        try {
            val db = getFirestore(context)
            serviceListenerRegistration?.remove()

            serviceListenerRegistration = db.collection("services")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Listen failed on services: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        scope.launch(Dispatchers.IO) {
                            for (doc in snapshot.documents) {
                                val data = doc.data ?: continue
                                try {
                                    val id = data["id"] as? String ?: doc.id
                                    val name = data["name"] as? String ?: continue
                                    val categoryStr = data["category"] as? String ?: "AUTRE"
                                    val desc = data["description"] as? String ?: ""
                                    val city = data["city"] as? String ?: "Bamako"
                                    val phone = data["phone"] as? String ?: ""
                                    val rating = (data["rating"] as? Number)?.toDouble() ?: 5.0
                                    val lat = (data["latitude"] as? Number)?.toDouble() ?: 12.6392
                                    val lng = (data["longitude"] as? Number)?.toDouble() ?: -8.0029
                                    val isOpen = data["isOpen"] as? Boolean ?: true
                                    val hours = data["hours"] as? String
                                    val servicesOffered = data["servicesOffered"] as? String

                                    val provider = ServiceProvider(
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
                                    onServiceReceived(provider)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error parsing service doc: ${doc.id}", e)
                                }
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start Firestore sync", e)
        }
    }

    fun pushServiceToFirestore(context: Context, service: ServiceProvider) {
        try {
            val db = getFirestore(context)
            val currentUid = Firebase.auth.currentUser?.uid

            val payload = hashMapOf(
                "id" to service.id,
                "name" to service.name,
                "category" to service.category.name,
                "description" to service.description,
                "city" to service.city,
                "phone" to service.phone,
                "rating" to service.rating,
                "latitude" to service.latitude,
                "longitude" to service.longitude,
                "isOpen" to service.isOpen,
                "hours" to (service.hours ?: ""),
                "servicesOffered" to (service.servicesOffered ?: "")
            )
            if (currentUid != null) {
                payload["ownerId"] = currentUid
            }

            db.collection("services").document(service.id).set(payload)
                .addOnSuccessListener {
                    Log.d(TAG, "Service pushed to Firestore: ${service.name}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Failed to push service to Firestore: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error pushing service to Firestore", e)
        }
    }

    fun pushReviewToFirestore(context: Context, review: Review) {
        try {
            val db = getFirestore(context)
            val currentUid = Firebase.auth.currentUser?.uid ?: return

            val payload = hashMapOf(
                "id" to review.id,
                "serviceId" to review.serviceId,
                "userId" to currentUid,
                "userName" to review.userName,
                "comment" to review.comment,
                "rating" to review.rating
            )

            db.collection("reviews").document(review.id).set(payload)
                .addOnSuccessListener {
                    Log.d(TAG, "Review pushed to Firestore: ${review.id}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Failed to push review to Firestore: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error pushing review to Firestore", e)
        }
    }

    fun pushBookingToFirestore(context: Context, booking: BookingEntity) {
        try {
            val db = getFirestore(context)
            val currentUid = Firebase.auth.currentUser?.uid ?: return

            val payload = hashMapOf(
                "id" to booking.id,
                "userId" to currentUid,
                "providerId" to booking.providerId,
                "providerName" to booking.providerName,
                "serviceType" to booking.serviceType,
                "clientName" to booking.clientName,
                "clientPhone" to booking.clientPhone,
                "date" to booking.date,
                "timeSlot" to (booking.timeSlot ?: ""),
                "status" to booking.status,
                "notes" to (booking.notes ?: "")
            )

            db.collection("bookings").document(booking.id).set(payload)
                .addOnSuccessListener {
                    Log.d(TAG, "Booking pushed to Firestore: ${booking.id}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Failed to push booking to Firestore: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error pushing booking to Firestore", e)
        }
    }
}
