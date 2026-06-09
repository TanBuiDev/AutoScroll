package com.personal.autoscroll.core.gesture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GesturePathTest {
    @Test
    fun verticalSwipeIsClippedAtScreenEdge() {
        val plan = GesturePlan(
            physicalDirection = PhysicalSwipeDirection.Up,
            distancePercent = 90,
            startXPercent = 50,
            startYPercent = 72,
            durationMillis = 600,
        )

        val path = plan.toGesturePath(width = 1_080f, height = 2_400f)

        assertEquals(540f, path.startX)
        assertEquals(1_728f, path.startY)
        assertEquals(540f, path.endX)
        assertEquals(0f, path.endY)
        assertTrue(path.clipped)
    }

    @Test
    fun horizontalSwipeUsesScreenWidthDistance() {
        val plan = GesturePlan(
            physicalDirection = PhysicalSwipeDirection.Left,
            distancePercent = 55,
            startXPercent = 70,
            startYPercent = 50,
            durationMillis = 600,
        )

        val path = plan.toGesturePath(width = 1_000f, height = 2_000f)

        assertEquals(700f, path.startX)
        assertEquals(150f, path.endX)
        assertEquals(1_000f, path.startY)
        assertEquals(1_000f, path.endY)
    }
}
