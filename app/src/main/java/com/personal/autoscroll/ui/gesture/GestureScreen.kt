package com.personal.autoscroll.ui.gesture

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.personal.autoscroll.R
import com.personal.autoscroll.domain.model.GestureAxis
import com.personal.autoscroll.domain.model.GestureConfig
import com.personal.autoscroll.domain.model.IntentDirection
import com.personal.autoscroll.ui.foundation.LocalizedFormatters

@Composable
fun GestureScreen(
    config: GestureConfig,
    showTitle: Boolean = true,
    onGestureAxisChanged: (GestureAxis) -> Unit = {},
    onIntentDirectionChanged: (IntentDirection) -> Unit = {},
    onGestureDistanceChanged: (Int) -> Unit = {},
    onSwipeDurationChanged: (Long) -> Unit = {},
    onStartXChanged: (Int) -> Unit = {},
    onStartYChanged: (Int) -> Unit = {},
    onInvertPhysicalDirectionChanged: (Boolean) -> Unit = {},
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (showTitle) {
            ScreenTitle(stringResource(R.string.nav_gesture))
        }
        Text(stringResource(R.string.gesture_navigation_target), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IntentDirection.entries.forEach {
                FilterChip(
                    selected = it == config.intentDirection,
                    onClick = { onIntentDirectionChanged(it) },
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
                    selected = it == config.axis,
                    onClick = { onGestureAxisChanged(it) },
                    label = {
                        Text(
                            if (it == GestureAxis.Vertical) {
                                stringResource(R.string.setting_vertical)
                            } else {
                                stringResource(R.string.setting_horizontal)
                            },
                        )
                    },
                )
            }
        }

        ToggleLine(
            label = stringResource(R.string.gesture_invert_direction),
            checked = config.invertPhysicalDirection,
            onCheckedChange = onInvertPhysicalDirectionChanged,
        )

        Text(stringResource(R.string.gesture_preview), color = MaterialTheme.colorScheme.onSurfaceVariant)
        GesturePreview(
            config = config,
            modifier = Modifier
                .fillMaxWidth()
                .height(164.dp),
        )

        Text(
            stringResource(R.string.gesture_distance, config.distancePercent),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Slider(
            value = config.distancePercent.toFloat(),
            onValueChange = { onGestureDistanceChanged(it.toInt()) },
            valueRange = 5f..95f,
        )

        Text(
            stringResource(R.string.gesture_start_x, config.startXPercent),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Slider(
            value = config.startXPercent.toFloat(),
            onValueChange = { onStartXChanged(it.toInt()) },
            valueRange = 0f..100f,
        )

        Text(
            stringResource(R.string.gesture_start_y, config.startYPercent),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Slider(
            value = config.startYPercent.toFloat(),
            onValueChange = { onStartYChanged(it.toInt()) },
            valueRange = 0f..100f,
        )

        Text(
            stringResource(
                R.string.gesture_swipe_time,
                LocalizedFormatters.seconds(config.swipeDurationMillis),
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Slider(
            value = config.swipeDurationMillis.toFloat(),
            onValueChange = { onSwipeDurationChanged(it.toLong()) },
            valueRange = GestureConfig.MIN_SWIPE_DURATION_MILLIS.toFloat()..
                GestureConfig.MAX_SWIPE_DURATION_MILLIS.toFloat(),
            steps = 17,
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun GesturePreview(
    config: GestureConfig,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val progress = remember { Animatable(0f) }
    val duration = config.swipeDurationMillis.toInt().coerceIn(200, 2_000)

    LaunchedEffect(config, duration) {
        while (true) {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = duration, easing = LinearEasing),
            )
            kotlinx.coroutines.delay(450L)
        }
    }

    Canvas(modifier = modifier) {
        val phoneWidth = size.width * 0.42f
        val phoneHeight = size.height * 0.88f
        val phoneLeft = (size.width - phoneWidth) / 2f
        val phoneTop = (size.height - phoneHeight) / 2f
        val phoneRect = Rect(
            offset = Offset(phoneLeft, phoneTop),
            size = Size(phoneWidth, phoneHeight),
        )
        drawRoundRect(
            color = colors.surface,
            topLeft = phoneRect.topLeft,
            size = phoneRect.size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(28.dp.toPx(), 28.dp.toPx()),
        )
        drawRoundRect(
            color = colors.outline.copy(alpha = 0.35f),
            topLeft = phoneRect.topLeft,
            size = phoneRect.size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(28.dp.toPx(), 28.dp.toPx()),
            style = Stroke(width = 1.5.dp.toPx()),
        )

        val start = Offset(
            x = phoneRect.left + phoneRect.width * config.startXPercent / 100f,
            y = phoneRect.top + phoneRect.height * config.startYPercent / 100f,
        )
        val distance = if (config.axis == GestureAxis.Vertical) {
            phoneRect.height * config.distancePercent / 100f
        } else {
            phoneRect.width * config.distancePercent / 100f
        }
        val nextDirection = if (config.invertPhysicalDirection) {
            config.intentDirection != IntentDirection.NextItem
        } else {
            config.intentDirection == IntentDirection.NextItem
        }
        val rawEnd = when (config.axis) {
            GestureAxis.Vertical -> start.copy(
                y = if (nextDirection) start.y - distance else start.y + distance,
            )
            GestureAxis.Horizontal -> start.copy(
                x = if (nextDirection) start.x - distance else start.x + distance,
            )
        }
        val end = Offset(
            x = rawEnd.x.coerceIn(phoneRect.left, phoneRect.right),
            y = rawEnd.y.coerceIn(phoneRect.top, phoneRect.bottom),
        )
        val hand = Offset(
            x = start.x + (end.x - start.x) * progress.value,
            y = start.y + (end.y - start.y) * progress.value,
        )

        drawLine(
            color = colors.primary.copy(alpha = 0.28f),
            start = start,
            end = end,
            strokeWidth = 10.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawCircle(color = colors.primary, radius = 7.dp.toPx(), center = start)
        drawCircle(color = colors.primary, radius = 7.dp.toPx(), center = end)
        drawCircle(color = colors.primaryContainer, radius = 19.dp.toPx(), center = hand)
        drawCircle(color = colors.primary, radius = 11.dp.toPx(), center = hand)
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
private fun ScreenTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
    )
}
