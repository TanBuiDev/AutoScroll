package com.personal.autoscroll.core.overlay

data class OverlayActions(
    val onStartStop: () -> Unit,
    val onNext: () -> Unit,
    val onPrevious: () -> Unit,
    val onSettings: () -> Unit,
)
