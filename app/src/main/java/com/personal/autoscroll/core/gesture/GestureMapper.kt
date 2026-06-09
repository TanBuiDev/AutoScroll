package com.personal.autoscroll.core.gesture

import com.personal.autoscroll.domain.model.GestureAxis
import com.personal.autoscroll.domain.model.GestureConfig
import com.personal.autoscroll.domain.model.GestureConfig.Companion.MAX_SWIPE_DURATION_MILLIS
import com.personal.autoscroll.domain.model.GestureConfig.Companion.MIN_SWIPE_DURATION_MILLIS
import com.personal.autoscroll.domain.model.IntentDirection
import javax.inject.Inject

class GestureMapper @Inject constructor() {
    fun map(config: GestureConfig): GesturePlan {
        val baseDirection = when (config.axis) {
            GestureAxis.Vertical -> when (config.intentDirection) {
                IntentDirection.NextItem -> PhysicalSwipeDirection.Up
                IntentDirection.PreviousItem -> PhysicalSwipeDirection.Down
            }
            GestureAxis.Horizontal -> when (config.intentDirection) {
                IntentDirection.NextItem -> PhysicalSwipeDirection.Left
                IntentDirection.PreviousItem -> PhysicalSwipeDirection.Right
            }
        }

        val physicalDirection = if (config.invertPhysicalDirection) {
            baseDirection.opposite()
        } else {
            baseDirection
        }

        return GesturePlan(
            physicalDirection = physicalDirection,
            distancePercent = config.distancePercent.coerceIn(5, 95),
            startXPercent = config.startXPercent.coerceIn(0, 100),
            startYPercent = config.startYPercent.coerceIn(0, 100),
            durationMillis = config.swipeDurationMillis.coerceIn(
                MIN_SWIPE_DURATION_MILLIS,
                MAX_SWIPE_DURATION_MILLIS,
            ),
        )
    }

    private fun PhysicalSwipeDirection.opposite(): PhysicalSwipeDirection = when (this) {
        PhysicalSwipeDirection.Up -> PhysicalSwipeDirection.Down
        PhysicalSwipeDirection.Down -> PhysicalSwipeDirection.Up
        PhysicalSwipeDirection.Left -> PhysicalSwipeDirection.Right
        PhysicalSwipeDirection.Right -> PhysicalSwipeDirection.Left
    }

}
