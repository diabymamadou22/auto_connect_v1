package com.example.autoconnect.data.model

data class OfferedService(
    val id: String,
    val providerId: String,
    val providerName: String,
    val title: String,
    val description: String,
    val priceCfa: Int,
    val durationMinutes: String = "45 min",
    val category: String = "Mécanique",
    val isAvailable: Boolean = true
)
