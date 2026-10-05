package com.example.autoconnect.data.model

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class ServiceProvider(
    val id: String,
    val name: String,
    val category: ServiceCategory,
    val description: String,
    val city: String,
    val phone: String,
    val rating: Double,
    val latitude: Double,
    val longitude: Double,
    val isOpen: Boolean = true,
    val isFavorite: Boolean = false,
    val isMine: Boolean = false,
    val hours: String? = null,
    val servicesOffered: String? = null
) {
    fun getDistance(userLat: Double, userLng: Double): Double {
        val earthRadius = 6371.0 // km
        val dLat = Math.toRadians(latitude - userLat)
        val dLng = Math.toRadians(longitude - userLng)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(userLat)) * cos(Math.toRadians(latitude)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }
}
