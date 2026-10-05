package com.example.autoconnect.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.TireRepair
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class ServiceCategory(
    val title: String,
    val icon: ImageVector,
    val color: Color
) {
    PIECES(
        title = "Pièces auto",
        icon = Icons.Default.Build,
        color = Color(0xFF4CAF50)
    ),
    MECANICIEN(
        title = "Mécaniciens",
        icon = Icons.Default.Handyman,
        color = Color(0xFF2196F3)
    ),
    PNEUMATIQUE(
        title = "Pneumatique",
        icon = Icons.Default.TireRepair,
        color = Color(0xFFFF9800)
    ),
    AUTRE(
        title = "Autres services",
        icon = Icons.Default.MoreHoriz,
        color = Color(0xFF9C27B0)
    );

    companion object {
        fun fromString(value: String): ServiceCategory {
            return try {
                valueOf(value.uppercase())
            } catch (e: Exception) {
                when (value.lowercase()) {
                    "pieces", "pièces auto" -> PIECES
                    "mecanicien", "mécaniciens" -> MECANICIEN
                    "pneumatique" -> PNEUMATIQUE
                    else -> AUTRE
                }
            }
        }
    }
}
