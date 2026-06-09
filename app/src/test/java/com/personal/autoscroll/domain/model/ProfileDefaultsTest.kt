package com.personal.autoscroll.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileDefaultsTest {
    @Test
    fun videoFeedDefaultMatchesMvpDecision() {
        val profile = AppProfile.defaultForPackage(
            packageName = "com.example.video",
            appName = "Video App",
            presetType = PresetType.VideoFeed,
        )

        assertEquals(ScrollMode.UntilStop, profile.timingConfig.mode)
        assertEquals(IntentDirection.NextItem, profile.gestureConfig.intentDirection)
        assertEquals(GestureAxis.Vertical, profile.gestureConfig.axis)
        assertTrue(profile.timingConfig.delayMillis in 5_000L..8_000L)
        assertEquals(ProfileStatus.Untested, profile.profileStatus)
    }
}
