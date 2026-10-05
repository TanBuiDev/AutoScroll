package com.personal.autoscroll.domain.model

data class GlobalSettings(
    val overlayOpacity: Float,
    val overlaySize: OverlaySize,
    val compactPositionX: Int,
    val compactPositionY: Int,
    val showNextPrevious: Boolean,
    val autoCollapse: Boolean,
    val themeMode: ThemeMode,
    val languageMode: LanguageMode,
) {
    companion object {
        val Default = GlobalSettings(
            overlayOpacity = 0.92f,
            overlaySize = OverlaySize.Medium,
            compactPositionX = 24,
            compactPositionY = 240,
            showNextPrevious = true,
            autoCollapse = true,
            themeMode = ThemeMode.System,
            languageMode = LanguageMode.System,
        )
    }
}
