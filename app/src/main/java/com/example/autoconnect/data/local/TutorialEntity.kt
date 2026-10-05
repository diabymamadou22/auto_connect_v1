package com.example.autoconnect.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tutorials")
data class TutorialEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val difficulty: String,
    val estimatedTimeMinutes: Int,
    val toolsNeeded: String,
    val summary: String,
    val stepsListRaw: String,
    val safetyWarning: String,
    val isOfflineCached: Boolean = true
)
