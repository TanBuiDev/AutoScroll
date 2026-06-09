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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.personal.autoscroll.R
import com.personal.autoscroll.domain.model.ScrollMode
import com.personal.autoscroll.ui.foundation.LocalizedFormatters

@Composable
fun TimingScreen(showTitle: Boolean = true) {
    var mode by remember { mutableStateOf(ScrollMode.UntilStop) }
    var delaySeconds by remember { mutableFloatStateOf(6.5f) }
    var repeatCountText by remember { mutableStateOf("1") }
    var durationMinutesText by remember { mutableStateOf("30") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (showTitle) {
            Text(
                text = stringResource(R.string.nav_timing),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(ScrollMode.Repeat, ScrollMode.UntilStop, ScrollMode.Timer).forEach {
                FilterChip(
                    selected = it == mode,
                    onClick = { mode = it },
                    label = { Text(it.label(), maxLines = 2) },
                )
            }
        }

        Text(
            "${stringResource(R.string.label_delay)}: ${LocalizedFormatters.seconds(delaySeconds)}",
            color = MaterialTheme.colorScheme.onSurface,
        )
        Slider(value = delaySeconds, onValueChange = { delaySeconds = it }, valueRange = 0.5f..60f)

        if (mode == ScrollMode.Repeat) {
            OutlinedTextField(
                value = repeatCountText,
                onValueChange = { value -> repeatCountText = value.filter(Char::isDigit).take(4).ifEmpty { "1" } },
                label = { Text(stringResource(R.string.label_repeat_count)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        }

        if (mode == ScrollMode.Timer) {
            OutlinedTextField(
                value = durationMinutesText,
                onValueChange = { value -> durationMinutesText = value.filter(Char::isDigit).take(4).ifEmpty { "1" } },
                label = { Text(stringResource(R.string.label_duration)) },
                suffix = { Text(stringResource(R.string.minutes_unit)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ScrollMode.label(): String = when (this) {
    ScrollMode.Once -> stringResource(R.string.mode_repeat)
    ScrollMode.Repeat -> stringResource(R.string.mode_repeat)
    ScrollMode.UntilStop -> stringResource(R.string.mode_until_stop)
    ScrollMode.Timer -> stringResource(R.string.mode_timer)
}
