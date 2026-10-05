package com.example.autoconnect.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE serviceId = :serviceId ORDER BY createdAt DESC")
    fun getReviewsForService(serviceId: String): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews WHERE serviceId = :serviceId")
    suspend fun getReviewsListForService(serviceId: String): List<ReviewEntity>

    @Query("SELECT * FROM reviews ORDER BY createdAt DESC")
    fun getAllReviews(): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)

    @Query("DELETE FROM reviews WHERE id = :id")
    suspend fun deleteReview(id: String)
}
