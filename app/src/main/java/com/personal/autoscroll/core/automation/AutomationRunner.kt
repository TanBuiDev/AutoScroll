package com.personal.autoscroll.core.automation

import com.personal.autoscroll.domain.model.ScrollMode
import com.personal.autoscroll.domain.model.TimingConfig
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

class AutomationRunner @Inject constructor() {
    suspend fun run(
        timingConfig: TimingConfig,
        performGesture: suspend () -> Boolean,
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
        performGesture: suspend () -> Boolean,
    ) {
        val count = (timingConfig.repeatCount ?: 1).coerceAtLeast(1)
        repeat(count) { index ->
            if (!performGesture()) return
            if (index < count - 1) {
                delay(timingConfig.delayMillis)
            }
        }
    }

    private suspend fun runUntilStop(
        timingConfig: TimingConfig,
        performGesture: suspend () -> Boolean,
    ) {
        while (currentCoroutineContext().isActive) {
            if (!performGesture()) return
            delay(timingConfig.delayMillis)
        }
    }

    private suspend fun runTimer(
        timingConfig: TimingConfig,
        performGesture: suspend () -> Boolean,
    ) {
        val durationMillis = timingConfig.durationMillis ?: return
        if (durationMillis <= 0L) return

        withTimeoutOrNull(durationMillis) {
            while (currentCoroutineContext().isActive) {
                if (!performGesture()) return@withTimeoutOrNull
                delay(timingConfig.delayMillis)
            }
        }
    }
}
