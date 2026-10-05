package com.example.autoconnect.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey val id: String,
    val providerId: String,
    val providerName: String,
    val serviceType: String,
    val clientName: String,
    val clientPhone: String,
    val date: String,
    val timeSlot: String,
    val status: String = "EN_ATTENTE", // EN_ATTENTE, CONFIRME, TERMINE, ANNULE
    val notes: String = ""
)
