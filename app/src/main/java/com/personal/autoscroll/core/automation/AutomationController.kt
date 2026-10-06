package com.personal.autoscroll.core.automation

import com.personal.autoscroll.core.gesture.GestureExecutor
import com.personal.autoscroll.core.accessibility.AutoScrollAccessibilityService
import com.personal.autoscroll.domain.model.GestureConfig
import com.personal.autoscroll.domain.model.TimingConfig
import com.personal.autoscroll.domain.model.ScrollMode
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AutomationController internal constructor(
    private val automationRunner: AutomationRunner,
    private val performGesture: suspend (GestureConfig) -> Boolean,
    private val serviceAvailable: () -> Boolean = { true },
    private val nowMillis: () -> Long = { System.nanoTime() / 1_000_000L },
) {
    @Inject constructor(
        automationRunner: AutomationRunner,
        gestureExecutor: GestureExecutor,
    ) : this(
        automationRunner,
        gestureExecutor::execute,
        { AutoScrollAccessibilityService.dispatcherOrNull() != null },
    )

    private val _state = MutableStateFlow<AutomationState>(AutomationState.Idle)
    val state: StateFlow<AutomationState> = _state.asStateFlow()

    private var job: Job? = null
    private var sessionId = 0L

    val isRunning: Boolean
        get() = job?.isActive == true

    fun start(
        scope: CoroutineScope,
        timingConfig: TimingConfig,
        gestureConfigProvider: () -> GestureConfig,
    ) {
        val runId = ++sessionId
        job?.cancel()
        var completed = 0
        val timerDuration = timingConfig.durationMillis?.takeIf { timingConfig.mode == ScrollMode.Timer }
        val timerStart = nowMillis() + timingConfig.startDelayMillis
        fun publishProgress() {
            if (runId == sessionId) {
                val remaining = timerDuration?.let { duration ->
                    (duration - (nowMillis() - timerStart).coerceAtLeast(0L)).coerceAtLeast(0L)
                }
                _state.value = AutomationState.Running(completed, remaining)
            }
        }
        val newJob = scope.launch(start = CoroutineStart.LAZY) {
            var reason = if (timingConfig.mode == ScrollMode.Timer) {
                AutomationStopReason.TimerExpired
            } else {
                AutomationStopReason.Completed
            }
            publishProgress()
            try {
                coroutineScope {
                    val ticker = if (timerDuration != null) launch {
                        while (isActive) {
                            delay(250L)
                            publishProgress()
                        }
                    } else null
                    try {
                        automationRunner.run(timingConfig) {
                            val succeeded = performGesture(gestureConfigProvider())
                            if (succeeded) {
                                completed += 1
                                publishProgress()
                            } else {
                                reason = if (serviceAvailable()) AutomationStopReason.GestureFailed
                                else AutomationStopReason.ServiceDisconnected
                            }
                            succeeded
                        }
                    } finally {
                        ticker?.cancel()
                    }
                }
            } catch (cancelled: CancellationException) {
                reason = AutomationStopReason.Interrupted
                throw cancelled
            } catch (_: Exception) {
                reason = AutomationStopReason.GestureFailed
            } finally {
                if (runId == sessionId) {
                    job = null
                    _state.value = AutomationState.Stopped(reason, completed)
                }
            }
        }
        job = newJob
        newJob.start()
    }

    fun stop(reason: AutomationStopReason = AutomationStopReason.UserStopped) {
        val running = _state.value as? AutomationState.Running
        val wasActive = isRunning
        sessionId += 1
        job?.cancel()
        job = null
        if (running != null || wasActive) {
            _state.value = AutomationState.Stopped(reason, running?.completedGestures ?: 0)
        }
    }
}
