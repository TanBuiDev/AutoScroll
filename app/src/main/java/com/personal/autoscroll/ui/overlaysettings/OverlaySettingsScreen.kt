package com.personal.autoscroll.ui.overlaysettings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.personal.autoscroll.ui.foundation.*
import com.personal.autoscroll.R
import com.personal.autoscroll.data.datastore.SettingsDataStore
import com.personal.autoscroll.domain.model.GlobalSettings
import com.personal.autoscroll.domain.model.OverlaySize
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.personal.autoscroll.ui.foundation.LocalizedFormatters

@Composable
fun OverlaySettingsScreen(viewModel: OverlaySettingsViewModel, showTitle: Boolean = true) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (showTitle) {
            Text(
                text = stringResource(R.string.nav_overlay),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        Text(
            "${stringResource(R.string.label_opacity)}: ${LocalizedFormatters.percent(settings.overlayOpacity)}",
            color = MaterialTheme.colorScheme.onSurface,
        )
        Slider(
            value = settings.overlayOpacity,
            onValueChange = viewModel::updateOverlayOpacity,
            valueRange = 0.4f..1f,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OverlaySize.entries.forEach {
                FilterChip(
                    selected = it == settings.overlaySize,
                    onClick = { viewModel.updateOverlaySize(it) },
                    label = { Text(stringResource(it.labelRes())) },
                )
            }
        }

        ToggleLine(
            stringResource(R.string.overlay_show_next_previous),
            settings.showNextPrevious,
            viewModel::updateShowNextPrevious,
        )
        ToggleLine(
            stringResource(R.string.overlay_auto_collapse),
            settings.autoCollapse,
            viewModel::updateAutoCollapse,
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ToggleLine(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Switch(checked = checked, onCheckedChange = onCheckedChange)
        Text(label, color = MaterialTheme.colorScheme.onSurface, maxLines = 2)
    }
}

@HiltViewModel
class OverlaySettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
) : ViewModel() {
    val settings = settingsDataStore.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GlobalSettings.Default,
    )

    fun updateOverlayOpacity(opacity: Float) {
        viewModelScope.launch {
            settingsDataStore.updateOverlayOpacity(opacity)
        }
    }

    fun updateOverlaySize(size: OverlaySize) {
        viewModelScope.launch {
            settingsDataStore.updateOverlaySize(size)
        }
    }

    fun updateShowNextPrevious(show: Boolean) {
        viewModelScope.launch {
            settingsDataStore.updateShowNextPrevious(show)
        }
    }

    fun updateAutoCollapse(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.updateAutoCollapse(enabled)
        }
    }
}
