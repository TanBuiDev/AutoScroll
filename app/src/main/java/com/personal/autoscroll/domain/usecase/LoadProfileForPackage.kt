package com.personal.autoscroll.domain.usecase

import com.personal.autoscroll.data.repository.ProfileRepository
import com.personal.autoscroll.domain.model.AppProfile
import com.personal.autoscroll.domain.model.PresetType
import javax.inject.Inject

class LoadProfileForPackage @Inject constructor(
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke(
        packageName: String,
        appName: String,
        presetType: PresetType = PresetType.VideoFeed,
    ): AppProfile = profileRepository.getProfile(packageName)
        ?: AppProfile.defaultForPackage(
            packageName = packageName,
            appName = appName,
            presetType = presetType,
        )
}
