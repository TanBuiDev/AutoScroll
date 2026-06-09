package com.personal.autoscroll.ui.advanced

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.personal.autoscroll.R
import com.personal.autoscroll.data.datastore.SettingsDataStore
import com.personal.autoscroll.domain.model.LanguageMode
import com.personal.autoscroll.domain.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Composable
fun AdvancedScreen(viewModel: AdvancedViewModel, showTitle: Boolean = true) {
    var screenshot by remember { mutableStateOf(false) }
    var recording by remember { mutableStateOf(false) }
    var audio by remember { mutableStateOf(false) }
    var boot by remember { mutableStateOf(false) }
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val languageMode by viewModel.languageMode.collectAsStateWithLifecycle()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (showTitle) {
            Text(
                text = stringResource(R.string.nav_advanced),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        Text(
            text = stringResource(R.string.advanced_description),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(stringResource(R.string.setting_theme), color = MaterialTheme.colorScheme.onSurface)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeMode.entries.forEach { mode ->
                FilterChip(
                    selected = mode == themeMode,
                    onClick = { viewModel.updateThemeMode(mode) },
                    label = { Text(mode.label(), maxLines = 2) },
                )
            }
        }
        Text(stringResource(R.string.setting_language), color = MaterialTheme.colorScheme.onSurface)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LanguageMode.entries.forEach { mode ->
                FilterChip(
                    selected = mode == languageMode,
                    onClick = { viewModel.updateLanguageMode(mode) },
                    label = { Text(mode.label(), maxLines = 2) },
                )
            }
        }
        ToggleLine(stringResource(R.string.advanced_screenshot), screenshot) { screenshot = it }
        ToggleLine(stringResource(R.string.advanced_recording), recording) { recording = it }
        ToggleLine(stringResource(R.string.advanced_audio), audio) { audio = it }
        ToggleLine(stringResource(R.string.advanced_boot), boot) { boot = it }
        Spacer(Modifier.height(24.dp))
    }
}

@HiltViewModel
class AdvancedViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
) : ViewModel() {
    val themeMode = settingsDataStore.settings
        .map { it.themeMode }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeMode.System,
        )
    val languageMode = settingsDataStore.settings
        .map { it.languageMode }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LanguageMode.System,
        )

    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            settingsDataStore.updateThemeMode(themeMode)
        }
    }

    fun updateLanguageMode(languageMode: LanguageMode) {
        viewModelScope.launch {
            settingsDataStore.updateLanguageMode(languageMode)
        }
    }
}

@Composable
private fun ToggleLine(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Switch(checked = checked, onCheckedChange = onCheckedChange)
        Text(label, color = MaterialTheme.colorScheme.onSurface, maxLines = 2)
    }
}

@Composable
private fun ThemeMode.label(): String = when (this) {
    ThemeMode.System -> stringResource(R.string.theme_system)
    ThemeMode.Light -> stringResource(R.string.theme_light)
    ThemeMode.Dark -> stringResource(R.string.theme_dark)
}

@Composable
private fun LanguageMode.label(): String = when (this) {
    LanguageMode.System -> stringResource(R.string.language_system)
    LanguageMode.English -> stringResource(R.string.language_english)
    LanguageMode.Vietnamese -> stringResource(R.string.language_vietnamese)
}
