package com.personal.autoscroll.core.automation

sealed interface AutomationState {
    data object Idle : AutomationState
    data class Running(
        val completedGestures: Int,
        val remainingMillis: Long? = null,
    ) : AutomationState

    data class Stopped(
        val reason: AutomationStopReason,
        val completedGestures: Int,
    ) : AutomationState
}

enum class AutomationStopReason {
    Completed,
    TimerExpired,
    UserStopped,
    AppChanged,
    ProfileDisabled,
    ServiceDisconnected,
    GestureFailed,
    Interrupted,
}
