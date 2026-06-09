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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.personal.autoscroll.R
import com.personal.autoscroll.domain.model.OverlaySize
import com.personal.autoscroll.ui.foundation.LocalizedFormatters

@Composable
fun OverlaySettingsScreen(showTitle: Boolean = true) {
    var opacity by remember { mutableFloatStateOf(0.92f) }
    var size by remember { mutableStateOf(OverlaySize.Medium) }
    var showNextPrevious by remember { mutableStateOf(true) }
    var autoCollapse by remember { mutableStateOf(true) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (showTitle) {
            Text(
                text = stringResource(R.string.nav_overlay),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        Text(
            "${stringResource(R.string.label_opacity)}: ${LocalizedFormatters.percent(opacity)}",
            color = MaterialTheme.colorScheme.onSurface,
        )
        Slider(value = opacity, onValueChange = { opacity = it }, valueRange = 0.4f..1f)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OverlaySize.entries.forEach {
                FilterChip(
                    selected = it == size,
                    onClick = { size = it },
                    label = { Text(it.toString()) },
                )
            }
        }

        ToggleLine(stringResource(R.string.overlay_show_next_previous), showNextPrevious) { showNextPrevious = it }
        ToggleLine(stringResource(R.string.overlay_auto_collapse), autoCollapse) { autoCollapse = it }
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
