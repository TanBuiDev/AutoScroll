package com.personal.autoscroll.domain.model

enum class IntentDirection {
    NextItem,
    PreviousItem,
}

enum class GestureAxis {
    Vertical,
    Horizontal,
}

data class GestureConfig(
    val intentDirection: IntentDirection,
    val axis: GestureAxis,
    val distancePercent: Int,
    val startXPercent: Int,
    val startYPercent: Int,
    val swipeDurationMillis: Long,
    val invertPhysicalDirection: Boolean,
) {
    companion object {
        const val MIN_SWIPE_DURATION_MILLIS: Long = 200L
        const val MAX_SWIPE_DURATION_MILLIS: Long = 2_000L
        const val DEFAULT_SWIPE_DURATION_MILLIS: Long = 600L

        fun videoFeedDefault(): GestureConfig = GestureConfig(
            intentDirection = IntentDirection.NextItem,
            axis = GestureAxis.Vertical,
            distancePercent = 55,
            startXPercent = 50,
            startYPercent = 72,
            swipeDurationMillis = DEFAULT_SWIPE_DURATION_MILLIS,
            invertPhysicalDirection = false,
        )
    }
}
