package com.personal.autoscroll.domain.model

fun AppProfile.withConfigurationUpdate(
    transform: AppProfile.() -> AppProfile,
): AppProfile {
    val updated = transform()
    if (updated == this) return this

    val invalidatesTest =
        gestureConfig != updated.gestureConfig ||
            timingConfig != updated.timingConfig ||
            presetType != updated.presetType

    return if (
        profileStatus == ProfileStatus.Tested &&
        updated.profileStatus == ProfileStatus.Tested &&
        invalidatesTest
    ) {
        updated.copy(profileStatus = ProfileStatus.NeedsReview)
    } else {
        updated
    }
}

fun AppProfile.withPreset(presetType: PresetType): AppProfile {
    if (this.presetType == presetType) return this

    if (presetType == PresetType.Custom) {
        return copy(presetType = presetType)
    }

    val defaults = AppProfile.defaultForPackage(
        packageName = packageName,
        appName = appName,
        presetType = presetType,
        nowMillis = createdAt,
    )
    return copy(
        presetType = presetType,
        gestureConfig = defaults.gestureConfig,
        timingConfig = defaults.timingConfig,
    )
}
