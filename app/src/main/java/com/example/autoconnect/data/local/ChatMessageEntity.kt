package com.example.autoconnect.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val providerId: String,
    val providerName: String,
    val sender: String, // "user" or "mechanic"
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isQuote: Boolean = false,
    val quoteAmount: String = "",
    val mediaUrl: String = ""
)
