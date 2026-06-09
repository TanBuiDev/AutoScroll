package com.personal.autoscroll.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class OverlayConfigSettingsTest {
    @Test
    fun applyGlobalSettingsUpdatesOnlyGlobalOverlayFields() {
        val current = OverlayConfig.default().copy(
            opacity = 0.4f,
            size = OverlaySize.Small,
            orientation = OverlayOrientation.Horizontal,
            showNextPrevious = false,
            autoCollapse = false,
        )
        val settings = GlobalSettings.Default.copy(
            overlayOpacity = 0.76f,
            overlaySize = OverlaySize.Large,
        )

        val updated = current.applyGlobalSettings(settings)

        assertEquals(0.76f, updated.opacity)
        assertEquals(OverlaySize.Large, updated.size)
        assertEquals(OverlayOrientation.Horizontal, updated.orientation)
        assertEquals(false, updated.showNextPrevious)
        assertEquals(false, updated.autoCollapse)
    }
}
