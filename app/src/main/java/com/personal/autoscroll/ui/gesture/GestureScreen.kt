package com.personal.autoscroll.ui.gesture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.personal.autoscroll.R
import com.personal.autoscroll.domain.model.GestureAxis
import com.personal.autoscroll.domain.model.IntentDirection
import com.personal.autoscroll.ui.foundation.LocalizedFormatters

@Composable
fun GestureScreen(showTitle: Boolean = true) {
    var direction by remember { mutableStateOf(IntentDirection.NextItem) }
    var axis by remember { mutableStateOf(GestureAxis.Vertical) }
    var distance by remember { mutableFloatStateOf(55f) }
    var swipeDuration by remember { mutableFloatStateOf(600f) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (showTitle) {
            ScreenTitle(stringResource(R.string.nav_gesture))
        }
        Text(stringResource(R.string.setting_direction), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IntentDirection.entries.forEach {
                FilterChip(
                    selected = it == direction,
                    onClick = { direction = it },
                    label = {
                        Text(
                            if (it == IntentDirection.NextItem) {
                                stringResource(R.string.direction_next_item)
                            } else {
                                stringResource(R.string.direction_previous_item)
                            },
                            maxLines = 2,
                        )
                    },
                )
            }
        }

        Text(stringResource(R.string.gesture_physical_mapping), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GestureAxis.entries.forEach {
                FilterChip(
                    selected = it == axis,
                    onClick = { axis = it },
                    label = { Text(it.toString()) },
                )
            }
        }

        Text(stringResource(R.string.gesture_distance, distance.toInt()), color = MaterialTheme.colorScheme.onSurface)
        Slider(value = distance, onValueChange = { distance = it }, valueRange = 10f..90f)

        Text(
            stringResource(R.string.gesture_swipe_time, LocalizedFormatters.seconds(swipeDuration.toLong())),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Slider(value = swipeDuration, onValueChange = { swipeDuration = it }, valueRange = 200f..2000f, steps = 17)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ScreenTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
    )
}
