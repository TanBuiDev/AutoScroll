package com.personal.autoscroll.core.automation

import com.personal.autoscroll.domain.model.ScrollMode
import com.personal.autoscroll.domain.model.TimingConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AutomationRunnerTest {
    private val runner = AutomationRunner()

    @Test
    fun onceRunsOneGesture() = runTest {
        var count = 0

        runner.run(timing(mode = ScrollMode.Once)) { count++ }

        assertEquals(1, count)
    }

    @Test
    fun repeatRunsConfiguredCount() = runTest {
        var count = 0

        runner.run(timing(mode = ScrollMode.Repeat, repeatCount = 3)) { count++ }

        assertEquals(3, count)
    }

    @Test
    fun timerStopsAfterDuration() = runTest {
        var count = 0

        runner.run(timing(mode = ScrollMode.Timer, delayMillis = 100, durationMillis = 250)) {
            count++
        }

        assertEquals(3, count)
    }

    @Test
    fun untilStopRunsUntilCancelled() = runTest {
        var count = 0
        val job = launch {
            runner.run(timing(mode = ScrollMode.UntilStop, delayMillis = 100)) {
                count++
            }
        }

        advanceTimeBy(250)
        job.cancelAndJoin()

        assertEquals(3, count)
    }

    private fun timing(
        mode: ScrollMode,
        delayMillis: Long = 100,
        repeatCount: Int? = null,
        durationMillis: Long? = null,
    ): TimingConfig = TimingConfig(
        mode = mode,
        delayMillis = delayMillis,
        startDelayMillis = 0,
        repeatCount = repeatCount,
        durationMillis = durationMillis,
        stopOnAppChange = true,
    )
}
