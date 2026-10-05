package com.personal.autoscroll.core.automation

import com.personal.autoscroll.core.gesture.GestureExecutor
import com.personal.autoscroll.domain.model.GestureConfig
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
    private val gestureExecutor: GestureExecutor,
) {
    private val _state = MutableStateFlow<AutomationState>(AutomationState.Idle)
    val state: StateFlow<AutomationState> = _state.asStateFlow()

    private var job: Job? = null

    val isRunning: Boolean
        get() = job?.isActive == true

    fun start(
        scope: CoroutineScope,
        timingConfig: TimingConfig,
        gestureConfigProvider: () -> GestureConfig,
    ) {
        stop()
        var completed = 0
        job = scope.launch {
            _state.value = AutomationState.Running(completedGestures = completed)
            try {
                automationRunner.run(timingConfig) {
                    val succeeded = gestureExecutor.execute(gestureConfigProvider())
                    if (succeeded) {
                        completed += 1
                        _state.value = AutomationState.Running(completedGestures = completed)
                    }
                    succeeded
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
