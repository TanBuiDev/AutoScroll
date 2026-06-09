package com.personal.autoscroll.core.gesture

enum class PhysicalSwipeDirection {
    Up,
    Down,
    Left,
    Right,
}

data class GesturePlan(
    val physicalDirection: PhysicalSwipeDirection,
    val distancePercent: Int,
    val startXPercent: Int,
    val startYPercent: Int,
    val durationMillis: Long,
)
