package com.example.autoconnect.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface OfferedServiceDao {
    @Query("SELECT * FROM offered_services WHERE providerId = :providerId ORDER BY title ASC")
    fun getServicesForProvider(providerId: String): Flow<List<OfferedServiceEntity>>

    @Query("SELECT * FROM offered_services WHERE isAvailable = 1 ORDER BY title ASC")
    fun getAllAvailableServices(): Flow<List<OfferedServiceEntity>>

    @Query("SELECT * FROM offered_services ORDER BY title ASC")
    fun getAllServices(): Flow<List<OfferedServiceEntity>>

    @Query("SELECT * FROM offered_services WHERE id = :id")
    suspend fun getServiceById(id: String): OfferedServiceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: OfferedServiceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(services: List<OfferedServiceEntity>)

    @Update
    suspend fun updateService(service: OfferedServiceEntity)

    @Query("DELETE FROM offered_services WHERE id = :id")
    suspend fun deleteService(id: String)

    @Query("DELETE FROM offered_services WHERE providerId = :providerId")
    suspend fun deleteServicesForProvider(providerId: String)
}
