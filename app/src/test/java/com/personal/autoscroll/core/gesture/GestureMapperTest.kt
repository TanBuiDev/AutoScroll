package com.personal.autoscroll.core.gesture

import com.personal.autoscroll.domain.model.GestureAxis
import com.personal.autoscroll.domain.model.GestureConfig
import com.personal.autoscroll.domain.model.IntentDirection
import org.junit.Assert.assertEquals
import org.junit.Test

class GestureMapperTest {
    private val mapper = GestureMapper()

    @Test
    fun verticalNextMapsToSwipeUp() {
        val plan = mapper.map(config(axis = GestureAxis.Vertical, intent = IntentDirection.NextItem))

        assertEquals(PhysicalSwipeDirection.Up, plan.physicalDirection)
    }

    @Test
    fun verticalPreviousMapsToSwipeDown() {
        val plan = mapper.map(config(axis = GestureAxis.Vertical, intent = IntentDirection.PreviousItem))

        assertEquals(PhysicalSwipeDirection.Down, plan.physicalDirection)
    }

    @Test
    fun horizontalNextMapsToSwipeLeft() {
        val plan = mapper.map(config(axis = GestureAxis.Horizontal, intent = IntentDirection.NextItem))

        assertEquals(PhysicalSwipeDirection.Left, plan.physicalDirection)
    }

    @Test
    fun horizontalPreviousMapsToSwipeRight() {
        val plan = mapper.map(config(axis = GestureAxis.Horizontal, intent = IntentDirection.PreviousItem))

        assertEquals(PhysicalSwipeDirection.Right, plan.physicalDirection)
    }

    @Test
    fun invertPhysicalDirectionFlipsResult() {
        val plan = mapper.map(
            config(
                axis = GestureAxis.Vertical,
                intent = IntentDirection.NextItem,
                invert = true,
            ),
        )

        assertEquals(PhysicalSwipeDirection.Down, plan.physicalDirection)
    }

    @Test
    fun swipeDurationUsesConfiguredMilliseconds() {
        val plan = mapper.map(
            config(
                axis = GestureAxis.Vertical,
                intent = IntentDirection.NextItem,
                swipeDurationMillis = 700L,
            ),
        )

        assertEquals(700L, plan.durationMillis)
    }

    @Test
    fun swipeDurationIsClampedToUserSafeRange() {
        val tooFast = mapper.map(
            config(
                axis = GestureAxis.Vertical,
                intent = IntentDirection.NextItem,
                swipeDurationMillis = 50L,
            ),
        )
        val tooSlow = mapper.map(
            config(
                axis = GestureAxis.Vertical,
                intent = IntentDirection.NextItem,
                swipeDurationMillis = 5_000L,
            ),
        )

        assertEquals(200L, tooFast.durationMillis)
        assertEquals(2_000L, tooSlow.durationMillis)
    }

    private fun config(
        axis: GestureAxis,
        intent: IntentDirection,
        invert: Boolean = false,
        swipeDurationMillis: Long = 600L,
    ): GestureConfig = GestureConfig(
        intentDirection = intent,
        axis = axis,
        distancePercent = 55,
        startXPercent = 50,
        startYPercent = 72,
        swipeDurationMillis = swipeDurationMillis,
        invertPhysicalDirection = invert,
    )
}
