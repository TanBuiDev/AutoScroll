package com.personal.autoscroll.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.personal.autoscroll.domain.model.ThemeMode

private val DarkColorScheme: ColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color(0xFF07080A),
    secondary = Color(0xFF7DD3A7),
    background = Color(0xFF07080A),
    onBackground = Color.White,
    surface = Color(0xFF0F1115),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF181B21),
    onSurfaceVariant = Color(0xFFA8ACB3),
    error = Color(0xFFFF5E5E),
)

private val LightColorScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF1E5BFF),
    onPrimary = Color.White,
    secondary = Color(0xFF0F7A4D),
    background = Color(0xFFF7F8FA),
    onBackground = Color(0xFF101216),
    surface = Color.White,
    onSurface = Color(0xFF101216),
    surfaceVariant = Color(0xFFE8EBF0),
    onSurfaceVariant = Color(0xFF4E5663),
    error = Color(0xFFB3261E),
)

@Composable
fun AutoScrollTheme(
    themeMode: ThemeMode = ThemeMode.System,
    content: @Composable () -> Unit,
) {
    val useDarkTheme = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    MaterialTheme(
        colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme,
        content = content,
    )
}
