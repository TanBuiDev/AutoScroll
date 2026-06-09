package com.personal.autoscroll.domain.usecase

import com.personal.autoscroll.data.repository.ProfileRepository
import com.personal.autoscroll.domain.model.AppProfile
import javax.inject.Inject

class SaveCurrentAppProfile @Inject constructor(
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke(profile: AppProfile) {
        profileRepository.upsertProfile(
            profile.copy(updatedAt = System.currentTimeMillis()),
        )
    }
}
