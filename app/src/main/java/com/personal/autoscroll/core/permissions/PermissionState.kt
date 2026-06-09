package com.personal.autoscroll.core.permissions

data class PermissionState(
    val accessibilityEnabled: Boolean,
    val accessibilityConnected: Boolean,
    val overlayPermissionGranted: Boolean,
)
