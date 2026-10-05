package com.example.autoconnect.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceProviderDao {
    @Query("SELECT * FROM services")
    fun getAllServices(): Flow<List<ServiceProviderEntity>>

    @Query("SELECT * FROM services WHERE id = :id LIMIT 1")
    suspend fun getServiceById(id: String): ServiceProviderEntity?

    @Query("SELECT * FROM services WHERE category = :category")
    fun getServicesByCategory(category: String): Flow<List<ServiceProviderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: ServiceProviderEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(services: List<ServiceProviderEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(services: List<ServiceProviderEntity>)

    @Query("SELECT COUNT(*) FROM services")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM services WHERE category = :category")
    suspend fun getCountByCategory(category: String): Int

    @Query("SELECT * FROM services WHERE category = 'MECANICIEN' ORDER BY rating DESC")
    fun getMechanics(): Flow<List<ServiceProviderEntity>>

    @Query("SELECT * FROM services WHERE category = 'PIECES' ORDER BY rating DESC")
    fun getPartsShops(): Flow<List<ServiceProviderEntity>>

    @Update
    suspend fun updateService(service: ServiceProviderEntity)

    @Query("DELETE FROM services WHERE id = :id")
    suspend fun deleteService(id: String)
}
