package com.personal.autoscroll.core.automation

import com.personal.autoscroll.domain.model.TimingConfig
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AutomationController @Inject constructor(
    private val automationRunner: AutomationRunner,
) {
    private val _state = MutableStateFlow<AutomationState>(AutomationState.Idle)
    val state: StateFlow<AutomationState> = _state.asStateFlow()

    private var job: Job? = null

    val isRunning: Boolean
        get() = job?.isActive == true

    fun start(
        scope: CoroutineScope,
        timingConfig: TimingConfig,
        performGesture: suspend () -> Unit,
    ) {
        stop()
        var completed = 0
        job = scope.launch {
            _state.value = AutomationState.Running(completedGestures = completed)
            try {
                automationRunner.run(timingConfig) {
                    performGesture()
                    completed += 1
                    _state.value = AutomationState.Running(completedGestures = completed)
                }
            } finally {
                _state.value = AutomationState.Idle
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        _state.value = AutomationState.Idle
    }
}
