package com.personal.autoscroll.core.automation

import com.personal.autoscroll.domain.model.GestureConfig
import com.personal.autoscroll.domain.model.ScrollMode
import com.personal.autoscroll.domain.model.TimingConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AutomationControllerTest {
    private val gesture = GestureConfig.videoFeedDefault()
    private fun timing(mode: ScrollMode = ScrollMode.Once) = TimingConfig.videoFeedDefault().copy(
        mode = mode, delayMillis = 500L, repeatCount = 3, durationMillis = 2_000L,
    )

    @Test fun onceReportsCompletionAndCount() = runTest {
        val controller = AutomationController(AutomationRunner(), { true })
        controller.start(this, timing()) { gesture }
        runCurrent()
        assertEquals(AutomationState.Stopped(AutomationStopReason.Completed, 1), controller.state.value)
        assertFalse(controller.isRunning)
    }

    @Test fun failedDispatchReportsFailureWithoutCountingIt() = runTest {
        val controller = AutomationController(AutomationRunner(), { false })
        controller.start(this, timing()) { gesture }
        runCurrent()
        assertEquals(AutomationState.Stopped(AutomationStopReason.GestureFailed, 0), controller.state.value)
    }

    @Test fun unavailableServiceExplainsFailure() = runTest {
        val controller = AutomationController(AutomationRunner(), { false }, { false })
        controller.start(this, timing()) { gesture }
        runCurrent()
        assertEquals(AutomationState.Stopped(AutomationStopReason.ServiceDisconnected, 0), controller.state.value)
    }

    @Test fun cancelledOldRunCannotOverwriteRestartedRun() = runTest {
        val controller = AutomationController(AutomationRunner(), { delay(100L); true })
        controller.start(this, timing(ScrollMode.UntilStop)) { gesture }
        runCurrent()
        controller.start(this, timing(ScrollMode.UntilStop)) { gesture }
        runCurrent()
        assertTrue(controller.state.value is AutomationState.Running)
        advanceTimeBy(100L)
        runCurrent()
        assertEquals(1, (controller.state.value as AutomationState.Running).completedGestures)
        controller.stop(AutomationStopReason.AppChanged)
        runCurrent()
        assertEquals(AutomationState.Stopped(AutomationStopReason.AppChanged, 1), controller.state.value)
    }

    @Test fun timerShowsCountdownAndExpiry() = runTest {
        val controller = AutomationController(AutomationRunner(), { true }, { true }, { testScheduler.currentTime })
        controller.start(this, timing(ScrollMode.Timer).copy(startDelayMillis = 500L)) { gesture }
        runCurrent()
        assertEquals(2_000L, (controller.state.value as AutomationState.Running).remainingMillis)
        advanceTimeBy(1_500L)
        runCurrent()
        assertEquals(1_000L, (controller.state.value as AutomationState.Running).remainingMillis)
        advanceTimeBy(1_000L)
        runCurrent()
        assertEquals(AutomationStopReason.TimerExpired, (controller.state.value as AutomationState.Stopped).reason)
        assertFalse(controller.isRunning)
    }

    @Test fun stopBeforeScheduledRunStillReportsUserStop() = runTest {
        val controller = AutomationController(AutomationRunner(), { true })
        controller.start(this, timing(ScrollMode.UntilStop)) { gesture }
        controller.stop()
        runCurrent()
        assertEquals(AutomationState.Stopped(AutomationStopReason.UserStopped, 0), controller.state.value)
    }

    @Test fun failedRepeatRetainsOnlyCompletedGestures() = runTest {
        var attempts = 0
        val controller = AutomationController(AutomationRunner(), { ++attempts < 2 })
        controller.start(this, timing(ScrollMode.Repeat)) { gesture }
        advanceTimeBy(500L)
        runCurrent()
        assertEquals(AutomationState.Stopped(AutomationStopReason.GestureFailed, 1), controller.state.value)
    }

    @Test fun unexpectedGestureExceptionDoesNotLeaveRunActive() = runTest {
        val controller = AutomationController(AutomationRunner(), { throw IllegalStateException("test failure") })
        controller.start(this, timing()) { gesture }
        runCurrent()
        assertEquals(AutomationState.Stopped(AutomationStopReason.GestureFailed, 0), controller.state.value)
        assertFalse(controller.isRunning)
    }
}
