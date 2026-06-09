package com.personal.autoscroll.domain.model

data class GlobalSettings(
    val overlayOpacity: Float,
    val overlaySize: OverlaySize,
    val compactPositionX: Int,
    val compactPositionY: Int,
    val themeMode: ThemeMode,
) {
    companion object {
        val Default = GlobalSettings(
            overlayOpacity = 0.92f,
            overlaySize = OverlaySize.Medium,
            compactPositionX = 24,
            compactPositionY = 240,
            themeMode = ThemeMode.System,
        )
    }
}
