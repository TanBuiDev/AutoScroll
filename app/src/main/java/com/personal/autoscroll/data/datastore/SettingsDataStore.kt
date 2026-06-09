package com.personal.autoscroll.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.personal.autoscroll.domain.model.GlobalSettings
import com.personal.autoscroll.domain.model.OverlaySize
import com.personal.autoscroll.domain.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "auto_scroll_settings",
)

class SettingsDataStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    val settings: Flow<GlobalSettings> = context.settingsDataStore.data.map { preferences ->
        GlobalSettings(
            overlayOpacity = preferences[OVERLAY_OPACITY] ?: GlobalSettings.Default.overlayOpacity,
            overlaySize = preferences[OVERLAY_SIZE]?.let { OverlaySize.valueOf(it) }
                ?: GlobalSettings.Default.overlaySize,
            compactPositionX = preferences[COMPACT_POSITION_X]
                ?: GlobalSettings.Default.compactPositionX,
            compactPositionY = preferences[COMPACT_POSITION_Y]
                ?: GlobalSettings.Default.compactPositionY,
            themeMode = preferences[THEME_MODE]
                ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: GlobalSettings.Default.themeMode,
        )
    }

    suspend fun updateOverlayOpacity(opacity: Float) {
        context.settingsDataStore.edit { preferences ->
            preferences[OVERLAY_OPACITY] = opacity.coerceIn(0.4f, 1.0f)
        }
    }

    suspend fun updateOverlaySize(size: OverlaySize) {
        context.settingsDataStore.edit { preferences ->
            preferences[OVERLAY_SIZE] = size.name
        }
    }

    suspend fun updateCompactPosition(x: Int, y: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[COMPACT_POSITION_X] = x
            preferences[COMPACT_POSITION_Y] = y
        }
    }

    suspend fun updateThemeMode(themeMode: ThemeMode) {
        context.settingsDataStore.edit { preferences ->
            preferences[THEME_MODE] = themeMode.name
        }
    }

    private companion object {
        val OVERLAY_OPACITY = floatPreferencesKey("overlay_opacity")
        val OVERLAY_SIZE = stringPreferencesKey("overlay_size")
        val COMPACT_POSITION_X = intPreferencesKey("compact_position_x")
        val COMPACT_POSITION_Y = intPreferencesKey("compact_position_y")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }
}
