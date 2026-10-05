package com.example.autoconnect.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TutorialDao {
    @Query("SELECT * FROM tutorials")
    fun getAllTutorials(): Flow<List<TutorialEntity>>

    @Query("SELECT * FROM tutorials WHERE category = :category")
    fun getTutorialsByCategory(category: String): Flow<List<TutorialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTutorial(tutorial: TutorialEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tutorials: List<TutorialEntity>)
}
