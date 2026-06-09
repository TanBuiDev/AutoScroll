package com.personal.autoscroll.core.automation

import com.personal.autoscroll.domain.model.ScrollMode
import com.personal.autoscroll.domain.model.TimingConfig
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import javax.inject.Inject

class AutomationRunner @Inject constructor() {
    suspend fun run(
        timingConfig: TimingConfig,
        performGesture: suspend () -> Unit,
    ) {
        if (timingConfig.startDelayMillis > 0) {
            delay(timingConfig.startDelayMillis)
        }

        when (timingConfig.mode) {
            ScrollMode.Once -> performGesture()
            ScrollMode.Repeat -> runRepeat(timingConfig, performGesture)
            ScrollMode.UntilStop -> runUntilStop(timingConfig, performGesture)
            ScrollMode.Timer -> runTimer(timingConfig, performGesture)
        }
    }

    private suspend fun runRepeat(
        timingConfig: TimingConfig,
        performGesture: suspend () -> Unit,
    ) {
        val count = (timingConfig.repeatCount ?: 1).coerceAtLeast(1)
        repeat(count) { index ->
            performGesture()
            if (index < count - 1) {
                delay(timingConfig.delayMillis)
            }
        }
    }

    private suspend fun runUntilStop(
        timingConfig: TimingConfig,
        performGesture: suspend () -> Unit,
    ) {
        while (currentCoroutineContext().isActive) {
            performGesture()
            delay(timingConfig.delayMillis)
        }
    }

    private suspend fun runTimer(
        timingConfig: TimingConfig,
        performGesture: suspend () -> Unit,
    ) {
        val durationMillis = timingConfig.durationMillis ?: 0L
        var elapsedMillis = 0L
        while (currentCoroutineContext().isActive && elapsedMillis < durationMillis) {
            performGesture()
            delay(timingConfig.delayMillis)
            elapsedMillis += timingConfig.delayMillis
        }
    }
}
