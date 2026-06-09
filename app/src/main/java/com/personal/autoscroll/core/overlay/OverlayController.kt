package com.personal.autoscroll.core.overlay

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.personal.autoscroll.domain.model.OverlayConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class OverlayController @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val windowManager = context.getSystemService(WindowManager::class.java)
    private var compactOverlay: CompactOverlay? = null
    private var expandedOverlay: ExpandedOverlay? = null

    fun canDrawOverlays(): Boolean = Settings.canDrawOverlays(context)

    fun showCompact(config: OverlayConfig, actions: OverlayActions) {
        if (!canDrawOverlays() || compactOverlay != null) return

        val params = defaultParams(width = WindowManager.LayoutParams.WRAP_CONTENT)
        val overlay = CompactOverlay(
            context = context,
            windowManager = windowManager,
            layoutParams = params,
            overlayConfig = config,
            actions = actions,
        )
        compactOverlay = overlay
        windowManager.addView(overlay.view, params)
    }

    fun updateCompact(config: OverlayConfig) {
        compactOverlay?.updateConfig(config)
    }

    fun setRunning(isRunning: Boolean) {
        compactOverlay?.setRunning(isRunning)
    }

    fun toggleExpanded(profile: com.personal.autoscroll.domain.model.AppProfile, actions: ExpandedOverlayActions) {
        if (!canDrawOverlays()) return
        if (expandedOverlay == null) {
            val overlay = ExpandedOverlay(context, profile, actions)
            expandedOverlay = overlay
            windowManager.addView(overlay.view, defaultParams(width = 520))
        } else {
            hideExpanded()
        }
    }

    fun updateExpanded(profile: com.personal.autoscroll.domain.model.AppProfile) {
        expandedOverlay?.update(profile)
    }

    fun collapseExpanded() {
        hideExpanded()
    }

    fun hideAll() {
        hideExpanded()
        compactOverlay?.let { removeViewSafely(it.view) }
        compactOverlay = null
    }

    private fun hideExpanded() {
        expandedOverlay?.let { removeViewSafely(it.view) }
        expandedOverlay = null
    }

    private fun removeViewSafely(view: View) {
        runCatching { windowManager.removeView(view) }
    }

    private fun defaultParams(width: Int): WindowManager.LayoutParams =
        WindowManager.LayoutParams(
            width,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            android.graphics.PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 24
            y = 420
        }
}
