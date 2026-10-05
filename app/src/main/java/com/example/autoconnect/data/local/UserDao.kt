package com.example.autoconnect.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE LOWER(username) = LOWER(:username) LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("UPDATE users SET passwordHash = :passwordHash WHERE LOWER(username) = LOWER(:username)")
    suspend fun updatePassword(username: String, passwordHash: String)

    @Query("SELECT * FROM users WHERE role = :role")
    suspend fun getUsersByRole(role: String): List<UserEntity>

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: String)
}
