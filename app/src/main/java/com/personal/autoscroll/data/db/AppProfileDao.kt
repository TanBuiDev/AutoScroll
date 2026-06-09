package com.personal.autoscroll.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AppProfileDao {
    @Query("SELECT * FROM app_profiles ORDER BY appName COLLATE NOCASE")
    fun observeAllProfiles(): Flow<List<AppProfileEntity>>

    @Query("SELECT * FROM app_profiles WHERE packageName = :packageName LIMIT 1")
    fun observeProfile(packageName: String): Flow<AppProfileEntity?>

    @Query("SELECT * FROM app_profiles WHERE packageName = :packageName LIMIT 1")
    suspend fun getProfile(packageName: String): AppProfileEntity?

    @Upsert
    suspend fun upsertProfile(entity: AppProfileEntity)

    @Query("DELETE FROM app_profiles WHERE packageName = :packageName")
    suspend fun deleteProfile(packageName: String)
}
