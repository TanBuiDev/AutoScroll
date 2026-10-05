package com.personal.autoscroll.core.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import com.personal.autoscroll.core.gesture.GesturePlan
import com.personal.autoscroll.core.gesture.toGesturePath
import kotlin.coroutines.resume
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.suspendCancellableCoroutine

class GestureDispatcher(
    private val service: AccessibilityService,
) {
    private val dispatchMutex = Mutex()

    suspend fun dispatch(plan: GesturePlan): Boolean =
        dispatchMutex.withLock {
            dispatchUnlocked(plan)
        }

    private suspend fun dispatchUnlocked(plan: GesturePlan): Boolean =
        suspendCancellableCoroutine { continuation ->
            val gesture = plan.toGestureDescription()
            val accepted = service.dispatchGesture(
                gesture,
                object : AccessibilityService.GestureResultCallback() {
                    override fun onCompleted(gestureDescription: GestureDescription) {
                        if (continuation.isActive) {
                            continuation.resume(true)
                        }
                    }

                    override fun onCancelled(gestureDescription: GestureDescription) {
                        if (continuation.isActive) {
                            continuation.resume(false)
                        }
                    }
                },
                null,
            )
            if (!accepted && continuation.isActive) {
                continuation.resume(false)
            }
        }

    private fun GesturePlan.toGestureDescription(): GestureDescription {
        val displayMetrics = service.resources.displayMetrics
        val width = displayMetrics.widthPixels.toFloat()
        val height = displayMetrics.heightPixels.toFloat()
        val gesturePath = toGesturePath(width, height)

        val path = Path().apply {
            moveTo(gesturePath.startX, gesturePath.startY)
            lineTo(gesturePath.endX, gesturePath.endY)
        }

        return GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0L, durationMillis))
            .build()
    }
}
