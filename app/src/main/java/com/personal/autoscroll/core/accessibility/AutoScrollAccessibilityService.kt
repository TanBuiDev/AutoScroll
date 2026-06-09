package com.personal.autoscroll.core.accessibility

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class AutoScrollAccessibilityService : AccessibilityService() {
    private var gestureDispatcher: GestureDispatcher? = null

    override fun onCreate() {
        super.onCreate()
        attachService()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        attachService()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            AccessibilityServiceState.updateForegroundPackage(event.packageName?.toString())
        }
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        if (currentService === this) {
            currentService = null
        }
        AccessibilityServiceState.markDisconnected()
        Log.d(TAG, "Accessibility service destroyed")
        super.onDestroy()
    }

    companion object {
        private var currentService: AutoScrollAccessibilityService? = null
        private const val TAG = "AutoScrollA11yService"

        fun dispatcherOrNull(): GestureDispatcher? = currentService?.gestureDispatcher
    }

    private fun attachService() {
        gestureDispatcher = GestureDispatcher(this)
        currentService = this
        AccessibilityServiceState.markConnected()
        Log.d(TAG, "Accessibility service attached")
    }
}
