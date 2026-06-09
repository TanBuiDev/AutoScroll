package com.personal.autoscroll.core.automation

sealed interface AutomationState {
    data object Idle : AutomationState
    data class Running(
        val completedGestures: Int,
    ) : AutomationState
}
