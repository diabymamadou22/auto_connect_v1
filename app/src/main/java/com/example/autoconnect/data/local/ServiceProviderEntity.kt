package com.example.autoconnect.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.autoconnect.data.model.ServiceCategory
import com.example.autoconnect.data.model.ServiceProvider

@Entity(tableName = "services")
data class ServiceProviderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
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
    fun toDomainModel(): ServiceProvider {
        return ServiceProvider(
            id = id,
            name = name,
            category = ServiceCategory.fromString(category),
            description = description,
            city = city,
            phone = phone,
            rating = rating,
            latitude = latitude,
            longitude = longitude,
            isOpen = isOpen,
            isFavorite = isFavorite,
            isMine = isMine,
            hours = hours,
            servicesOffered = servicesOffered
        )
    }

    companion object {
        fun fromDomainModel(provider: ServiceProvider): ServiceProviderEntity {
            return ServiceProviderEntity(
                id = provider.id,
                name = provider.name,
                category = provider.category.name,
                description = provider.description,
                city = provider.city,
                phone = provider.phone,
                rating = provider.rating,
                latitude = provider.latitude,
                longitude = provider.longitude,
                isOpen = provider.isOpen,
                isFavorite = provider.isFavorite,
                isMine = provider.isMine,
                hours = provider.hours,
                servicesOffered = provider.servicesOffered
            )
        }
    }
}
