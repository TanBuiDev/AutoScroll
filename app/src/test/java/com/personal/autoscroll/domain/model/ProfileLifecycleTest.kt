package com.personal.autoscroll.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileLifecycleTest {
    private fun profile(status: ProfileStatus): AppProfile =
        AppProfile.defaultForPackage(
            packageName = "com.example.video",
            appName = "Video App",
            presetType = PresetType.VideoFeed,
            nowMillis = 123L,
        ).copy(profileStatus = status)

    @Test
    fun untestedConfigurationEditStaysUntested() {
        val updated = profile(ProfileStatus.Untested).withConfigurationUpdate {
            copy(timingConfig = timingConfig.copy(delayMillis = 2_000L))
        }

        assertEquals(ProfileStatus.Untested, updated.profileStatus)
    }

    @Test
    fun testedConfigurationEditNeedsReview() {
        val updated = profile(ProfileStatus.Tested).withConfigurationUpdate {
            copy(gestureConfig = gestureConfig.copy(distancePercent = 70))
        }

        assertEquals(ProfileStatus.NeedsReview, updated.profileStatus)
    }

    @Test
    fun testedOverlayOnlyEditStaysTested() {
        val updated = profile(ProfileStatus.Tested).withConfigurationUpdate {
            copy(overlayConfig = overlayConfig.copy(expandedPositionX = 120))
        }

        assertEquals(ProfileStatus.Tested, updated.profileStatus)
    }

    @Test
    fun presetChangeNeedsReviewAndAppliesDefaults() {
        val updated = profile(ProfileStatus.Tested).withConfigurationUpdate {
            withPreset(PresetType.HorizontalFeed)
        }

        assertEquals(ProfileStatus.NeedsReview, updated.profileStatus)
        assertEquals(PresetType.HorizontalFeed, updated.presetType)
        assertEquals(GestureAxis.Horizontal, updated.gestureConfig.axis)
    }
}
