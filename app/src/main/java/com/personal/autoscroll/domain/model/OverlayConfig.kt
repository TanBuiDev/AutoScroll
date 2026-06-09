package com.personal.autoscroll.domain.model

enum class OverlaySize {
    Small,
    Medium,
    Large,
}

enum class OverlayOrientation {
    Vertical,
    Horizontal,
}

data class OverlayConfig(
    val compactPositionX: Int,
    val compactPositionY: Int,
    val expandedPositionX: Int,
    val expandedPositionY: Int,
    val opacity: Float,
    val size: OverlaySize,
    val orientation: OverlayOrientation = OverlayOrientation.Vertical,
    val showNextPrevious: Boolean,
    val autoCollapse: Boolean,
) {
    companion object {
        fun default(): OverlayConfig = OverlayConfig(
            compactPositionX = 24,
            compactPositionY = 240,
            expandedPositionX = 24,
            expandedPositionY = 160,
            opacity = 0.92f,
            size = OverlaySize.Medium,
            orientation = OverlayOrientation.Vertical,
            showNextPrevious = true,
            autoCollapse = true,
        )
    }
}

fun OverlayConfig.applyGlobalSettings(settings: GlobalSettings): OverlayConfig =
    copy(
        opacity = settings.overlayOpacity,
        size = settings.overlaySize,
        compactPositionX = settings.compactPositionX,
        compactPositionY = settings.compactPositionY,
    )
