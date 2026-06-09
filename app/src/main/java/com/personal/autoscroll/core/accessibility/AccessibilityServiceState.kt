package com.personal.autoscroll.core.accessibility

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AccessibilityServiceState {
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _foregroundPackage = MutableStateFlow<String?>(null)
    val foregroundPackage: StateFlow<String?> = _foregroundPackage.asStateFlow()

    fun markConnected() {
        _isConnected.value = true
    }

    fun markDisconnected() {
        _isConnected.value = false
        _foregroundPackage.value = null
    }

    fun updateForegroundPackage(packageName: String?) {
        _foregroundPackage.value = packageName
    }
}
