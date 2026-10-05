package com.example.autoconnect.data.model

data class Review(
    val id: String,
    val serviceId: String,
    val userName: String,
    val comment: String,
    val rating: Double,
    val createdAt: String
)
