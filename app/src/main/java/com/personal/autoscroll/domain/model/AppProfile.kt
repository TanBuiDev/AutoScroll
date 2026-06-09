package com.personal.autoscroll.domain.model

enum class PresetType {
    VideoFeed,
    Reading,
    Webtoon,
    HorizontalFeed,
    Custom,
}

enum class ProfileStatus {
    Untested,
    Tested,
    NeedsReview,
}

data class AppProfile(
    val id: Long = 0L,
    val packageName: String,
    val appName: String,
    val enabled: Boolean,
    val presetType: PresetType,
    val profileStatus: ProfileStatus,
    val gestureConfig: GestureConfig,
    val timingConfig: TimingConfig,
    val overlayConfig: OverlayConfig,
    val createdAt: Long,
    val updatedAt: Long,
) {
    companion object {
        fun defaultForPackage(
            packageName: String,
            appName: String,
            presetType: PresetType,
            nowMillis: Long = System.currentTimeMillis(),
        ): AppProfile {
            val gestureConfig = when (presetType) {
                PresetType.HorizontalFeed -> GestureConfig.videoFeedDefault()
                    .copy(axis = GestureAxis.Horizontal)
                else -> GestureConfig.videoFeedDefault()
            }

            val timingConfig = when (presetType) {
                PresetType.Reading -> TimingConfig.videoFeedDefault()
                    .copy(delayMillis = 1_200L)
                PresetType.Webtoon -> TimingConfig.videoFeedDefault()
                    .copy(delayMillis = 1_600L)
                else -> TimingConfig.videoFeedDefault()
            }

            return AppProfile(
                packageName = packageName,
                appName = appName,
                enabled = true,
                presetType = presetType,
                profileStatus = ProfileStatus.Untested,
                gestureConfig = gestureConfig,
                timingConfig = timingConfig,
                overlayConfig = OverlayConfig.default(),
                createdAt = nowMillis,
                updatedAt = nowMillis,
            )
        }
    }
}
