package com.personal.autoscroll.core.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import com.personal.autoscroll.core.gesture.GesturePlan
import com.personal.autoscroll.core.gesture.PhysicalSwipeDirection
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class GestureDispatcher(
    private val service: AccessibilityService,
) {
    suspend fun dispatch(plan: GesturePlan): Boolean = suspendCancellableCoroutine { continuation ->
        val gesture = plan.toGestureDescription()
        service.dispatchGesture(
            gesture,
            object : AccessibilityService.GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription) {
                    continuation.resume(true)
                }

                override fun onCancelled(gestureDescription: GestureDescription) {
                    continuation.resume(false)
                }
            },
            null,
        )
    }

    private fun GesturePlan.toGestureDescription(): GestureDescription {
        val displayMetrics = service.resources.displayMetrics
        val width = displayMetrics.widthPixels.toFloat()
        val height = displayMetrics.heightPixels.toFloat()
        val startX = width * startXPercent / 100f
        val startY = height * startYPercent / 100f
        val distance = when (physicalDirection) {
            PhysicalSwipeDirection.Up,
            PhysicalSwipeDirection.Down -> height * distancePercent / 100f
            PhysicalSwipeDirection.Left,
            PhysicalSwipeDirection.Right -> width * distancePercent / 100f
        }

        val endX = when (physicalDirection) {
            PhysicalSwipeDirection.Left -> startX - distance
            PhysicalSwipeDirection.Right -> startX + distance
            else -> startX
        }.coerceIn(0f, width)

        val endY = when (physicalDirection) {
            PhysicalSwipeDirection.Up -> startY - distance
            PhysicalSwipeDirection.Down -> startY + distance
            else -> startY
        }.coerceIn(0f, height)

        val path = Path().apply {
            moveTo(startX.coerceIn(0f, width), startY.coerceIn(0f, height))
            lineTo(endX, endY)
        }

        return GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0L, durationMillis))
            .build()
    }
}
