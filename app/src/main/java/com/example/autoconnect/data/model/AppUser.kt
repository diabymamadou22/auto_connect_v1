package com.example.autoconnect.data.model

data class AppUser(
    val id: String,
    val username: String,
    val role: String // "admin", "prestataire", "client"
)
