package com.personal.autoscroll.data.repository

import com.personal.autoscroll.data.db.toDomain
import com.personal.autoscroll.data.db.toEntity
import com.personal.autoscroll.domain.model.AppProfile
import com.personal.autoscroll.domain.model.GestureAxis
import com.personal.autoscroll.domain.model.IntentDirection
import com.personal.autoscroll.domain.model.PresetType
import com.personal.autoscroll.domain.model.ProfileStatus
import com.personal.autoscroll.domain.model.ScrollMode
import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileRepositoryMapperTest {
    @Test
    fun profileRoundTripKeepsCoreConfiguration() {
        val profile = AppProfile.defaultForPackage(
            packageName = "com.example.video",
            appName = "Video App",
            presetType = PresetType.VideoFeed,
            nowMillis = 123L,
        )

        val restored = profile.toEntity().toDomain()

        assertEquals(profile.packageName, restored.packageName)
        assertEquals(profile.appName, restored.appName)
        assertEquals(ProfileStatus.Untested, restored.profileStatus)
        assertEquals(ScrollMode.UntilStop, restored.timingConfig.mode)
        assertEquals(IntentDirection.NextItem, restored.gestureConfig.intentDirection)
        assertEquals(GestureAxis.Vertical, restored.gestureConfig.axis)
        assertEquals(profile.gestureConfig.swipeDurationMillis, restored.gestureConfig.swipeDurationMillis)
        assertEquals(profile.timingConfig.delayMillis, restored.timingConfig.delayMillis)
        assertEquals(profile.overlayConfig.opacity, restored.overlayConfig.opacity, 0.001f)
    }
}
