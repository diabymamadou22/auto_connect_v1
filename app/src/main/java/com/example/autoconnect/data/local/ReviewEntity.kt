package com.example.autoconnect.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.autoconnect.data.model.Review

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val id: String,
    val serviceId: String,
    val userName: String,
    val comment: String,
    val rating: Double,
    val createdAt: String
) {
    fun toDomainModel(): Review {
        return Review(
            id = id,
            serviceId = serviceId,
            userName = userName,
            comment = comment,
            rating = rating,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromDomainModel(review: Review): ReviewEntity {
            return ReviewEntity(
                id = review.id,
                serviceId = review.serviceId,
                userName = review.userName,
                comment = review.comment,
                rating = review.rating,
                createdAt = review.createdAt
            )
        }
    }
}
