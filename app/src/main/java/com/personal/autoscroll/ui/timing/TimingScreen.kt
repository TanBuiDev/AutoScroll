package com.personal.autoscroll.ui.timing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.personal.autoscroll.R
import com.personal.autoscroll.domain.model.ScrollMode
import com.personal.autoscroll.domain.model.TimingConfig
import com.personal.autoscroll.ui.foundation.LocalizedFormatters

@Composable
fun TimingScreen(
    config: TimingConfig,
    showTitle: Boolean = true,
    onModeChanged: (ScrollMode) -> Unit = {},
    onDelayChanged: (Long) -> Unit = {},
    onStartDelayChanged: (Long) -> Unit = {},
    onRepeatCountChanged: (Int) -> Unit = {},
    onDurationChanged: (Long) -> Unit = {},
    onStopOnAppChangeChanged: (Boolean) -> Unit = {},
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (showTitle) {
            Text(
                text = stringResource(R.string.nav_timing),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        ScrollMode.entries.chunked(2).forEach { modes ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                modes.forEach { mode ->
                    FilterChip(
                        selected = mode == config.mode,
                        onClick = { onModeChanged(mode) },
                        label = { Text(mode.label(), maxLines = 2) },
                    )
                }
            }
        }

        Text(
            stringResource(R.string.label_delay) + ": " + LocalizedFormatters.seconds(config.delayMillis),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Slider(
            value = config.delayMillis.toFloat(),
            onValueChange = { onDelayChanged(it.toLong()) },
            valueRange = 500f..60_000f,
        )

        Text(
            stringResource(R.string.label_start_delay) + ": " + LocalizedFormatters.seconds(config.startDelayMillis),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Slider(
            value = config.startDelayMillis.toFloat(),
            onValueChange = { onStartDelayChanged(it.toLong()) },
            valueRange = 0f..30_000f,
        )

        if (config.mode == ScrollMode.Repeat) {
            OutlinedTextField(
                value = (config.repeatCount ?: 1).toString(),
                onValueChange = { value ->
                    val repeatCount = value.filter(Char::isDigit)
                        .take(4)
                        .toIntOrNull()
                        ?.coerceAtLeast(1)
                        ?: 1
                    onRepeatCountChanged(repeatCount)
                },
                label = { Text(stringResource(R.string.label_repeat_count)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        }

        if (config.mode == ScrollMode.Timer) {
            OutlinedTextField(
                value = ((config.durationMillis ?: 60_000L) / 60_000L).coerceAtLeast(1L).toString(),
                onValueChange = { value ->
                    val minutes = value.filter(Char::isDigit)
                        .take(4)
                        .toLongOrNull()
                        ?.coerceAtLeast(1L)
                        ?: 1L
                    onDurationChanged(minutes * 60_000L)
                },
                label = { Text(stringResource(R.string.label_duration)) },
                suffix = { Text(stringResource(R.string.minutes_unit)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        }

        ToggleLine(
            label = stringResource(R.string.timing_stop_on_app_change),
            checked = config.stopOnAppChange,
            onCheckedChange = onStopOnAppChangeChanged,
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ToggleLine(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Switch(checked = checked, onCheckedChange = onCheckedChange)
        Text(label, color = MaterialTheme.colorScheme.onSurface, maxLines = 2)
    }
}

@Composable
private fun ScrollMode.label(): String = when (this) {
    ScrollMode.Once -> stringResource(R.string.mode_once)
    ScrollMode.Repeat -> stringResource(R.string.mode_repeat)
    ScrollMode.UntilStop -> stringResource(R.string.mode_until_stop)
    ScrollMode.Timer -> stringResource(R.string.mode_timer)
}
