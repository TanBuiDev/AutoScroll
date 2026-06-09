package com.personal.autoscroll.data.repository

import com.personal.autoscroll.data.db.AppProfileDao
import com.personal.autoscroll.data.db.toDomain
import com.personal.autoscroll.data.db.toEntity
import com.personal.autoscroll.domain.model.AppProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val appProfileDao: AppProfileDao,
) {
    fun observeAllProfiles(): Flow<List<AppProfile>> =
        appProfileDao.observeAllProfiles().map { entities ->
            entities.map { it.toDomain() }
        }

    fun observeProfile(packageName: String): Flow<AppProfile?> =
        appProfileDao.observeProfile(packageName).map { it?.toDomain() }

    suspend fun getProfile(packageName: String): AppProfile? =
        appProfileDao.getProfile(packageName)?.toDomain()

    suspend fun upsertProfile(profile: AppProfile) {
        appProfileDao.upsertProfile(profile.toEntity())
    }

    suspend fun deleteProfile(packageName: String) {
        appProfileDao.deleteProfile(packageName)
    }
}
