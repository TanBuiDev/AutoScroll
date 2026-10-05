package com.personal.autoscroll.core.accessibility

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.personal.autoscroll.core.permissions.AccessibilityConsentStore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AutoScrollAccessibilityService : AccessibilityService() {
    @Inject lateinit var accessibilityConsentStore: AccessibilityConsentStore

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
        if (!hasAccessibilityConsent()) return
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

        fun dispatcherOrNull(): GestureDispatcher? =
            currentService?.takeIf { it.hasAccessibilityConsent() }?.gestureDispatcher
    }

    private fun hasAccessibilityConsent(): Boolean =
        ::accessibilityConsentStore.isInitialized && accessibilityConsentStore.isAccepted()

    private fun attachService() {
        gestureDispatcher = GestureDispatcher(this)
        currentService = this
        AccessibilityServiceState.markConnected()
        Log.d(TAG, "Accessibility service attached")
    }
}
