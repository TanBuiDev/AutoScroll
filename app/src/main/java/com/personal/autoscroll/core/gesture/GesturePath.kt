package com.personal.autoscroll.core.gesture

data class GesturePath(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val clipped: Boolean,
)

fun GesturePlan.toGesturePath(width: Float, height: Float): GesturePath {
    val startX = (width * startXPercent / 100f).coerceIn(0f, width)
    val startY = (height * startYPercent / 100f).coerceIn(0f, height)
    val distance = when (physicalDirection) {
        PhysicalSwipeDirection.Up,
        PhysicalSwipeDirection.Down -> height * distancePercent / 100f
        PhysicalSwipeDirection.Left,
        PhysicalSwipeDirection.Right -> width * distancePercent / 100f
    }

    val rawEndX = when (physicalDirection) {
        PhysicalSwipeDirection.Left -> startX - distance
        PhysicalSwipeDirection.Right -> startX + distance
        else -> startX
    }
    val rawEndY = when (physicalDirection) {
        PhysicalSwipeDirection.Up -> startY - distance
        PhysicalSwipeDirection.Down -> startY + distance
        else -> startY
    }
    val endX = rawEndX.coerceIn(0f, width)
    val endY = rawEndY.coerceIn(0f, height)

    return GesturePath(
        startX = startX,
        startY = startY,
        endX = endX,
        endY = endY,
        clipped = endX != rawEndX || endY != rawEndY,
    )
}
