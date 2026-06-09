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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import com.personal.autoscroll.domain.model.IntentDirection
import com.personal.autoscroll.ui.foundation.LocalizedFormatters

@Composable
fun GestureScreen(
    showTitle: Boolean = true,
    onGestureAxisChanged: (GestureAxis) -> Unit = {},
    onIntentDirectionChanged: (IntentDirection) -> Unit = {},
    onGestureDistanceChanged: (Int) -> Unit = {},
    onSwipeDurationChanged: (Long) -> Unit = {},
) {
    var direction by remember { mutableStateOf(IntentDirection.NextItem) }
    var axis by remember { mutableStateOf(GestureAxis.Vertical) }
    var distance by remember { mutableFloatStateOf(55f) }
    var swipeDuration by remember { mutableFloatStateOf(600f) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (showTitle) {
            ScreenTitle(stringResource(R.string.nav_gesture))
        }
        Text(stringResource(R.string.gesture_navigation_target), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IntentDirection.entries.forEach {
                FilterChip(
                    selected = it == direction,
                    onClick = {
                        direction = it
                        onIntentDirectionChanged(it)
                    },
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
                    onClick = {
                        axis = it
                        onGestureAxisChanged(it)
                    },
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

        Text(stringResource(R.string.gesture_preview), color = MaterialTheme.colorScheme.onSurfaceVariant)
        GesturePreview(
            axis = axis,
            direction = direction,
            distancePercent = distance.toInt(),
            swipeDurationMillis = swipeDuration.toLong(),
            modifier = Modifier
                .fillMaxWidth()
                .height(164.dp),
        )

        Text(stringResource(R.string.gesture_distance, distance.toInt()), color = MaterialTheme.colorScheme.onSurface)
        Slider(
            value = distance,
            onValueChange = {
                distance = it
                onGestureDistanceChanged(it.toInt())
            },
            valueRange = 10f..90f,
        )

        Text(
            stringResource(R.string.gesture_swipe_time, LocalizedFormatters.seconds(swipeDuration.toLong())),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Slider(
            value = swipeDuration,
            onValueChange = {
                swipeDuration = it
                onSwipeDurationChanged(it.toLong())
            },
            valueRange = 200f..2000f,
            steps = 17,
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun GesturePreview(
    axis: GestureAxis,
    direction: IntentDirection,
    distancePercent: Int,
    swipeDurationMillis: Long,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val progress = remember { Animatable(0f) }
    val duration = swipeDurationMillis.toInt().coerceIn(200, 2_000)

    LaunchedEffect(axis, direction, distancePercent, duration) {
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

        val maxDistance = if (axis == GestureAxis.Vertical) phoneHeight * 0.62f else phoneWidth * 0.62f
        val distance = maxDistance * (distancePercent / 90f)
        val center = phoneRect.center
        val start = when {
            axis == GestureAxis.Vertical && direction == IntentDirection.NextItem -> center.copy(y = center.y + distance / 2f)
            axis == GestureAxis.Vertical -> center.copy(y = center.y - distance / 2f)
            axis == GestureAxis.Horizontal && direction == IntentDirection.NextItem -> center.copy(x = center.x + distance / 2f)
            else -> center.copy(x = center.x - distance / 2f)
        }
        val end = when {
            axis == GestureAxis.Vertical && direction == IntentDirection.NextItem -> center.copy(y = center.y - distance / 2f)
            axis == GestureAxis.Vertical -> center.copy(y = center.y + distance / 2f)
            axis == GestureAxis.Horizontal && direction == IntentDirection.NextItem -> center.copy(x = center.x - distance / 2f)
            else -> center.copy(x = center.x + distance / 2f)
        }
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
private fun ScreenTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
    )
}
