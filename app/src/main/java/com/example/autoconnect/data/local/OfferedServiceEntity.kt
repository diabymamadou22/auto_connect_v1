package com.example.autoconnect.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.autoconnect.data.model.OfferedService

@Entity(tableName = "offered_services")
data class OfferedServiceEntity(
    @PrimaryKey val id: String,
    val providerId: String,
    val providerName: String,
    val title: String,
    val description: String,
    val priceCfa: Int,
    val durationMinutes: String = "45 min",
    val category: String = "Mécanique",
    val isAvailable: Boolean = true
) {
    fun toDomainModel(): OfferedService = OfferedService(
        id = id,
        providerId = providerId,
        providerName = providerName,
        title = title,
        description = description,
        priceCfa = priceCfa,
        durationMinutes = durationMinutes,
        category = category,
        isAvailable = isAvailable
    )

    companion object {
        fun fromDomainModel(service: OfferedService): OfferedServiceEntity = OfferedServiceEntity(
            id = service.id,
            providerId = service.providerId,
            providerName = service.providerName,
            title = service.title,
            description = service.description,
            priceCfa = service.priceCfa,
            durationMinutes = service.durationMinutes,
            category = service.category,
            isAvailable = service.isAvailable
        )
    }
}
