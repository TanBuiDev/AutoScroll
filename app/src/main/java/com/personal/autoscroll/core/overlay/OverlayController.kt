package com.personal.autoscroll.core.overlay

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.personal.autoscroll.core.localization.AppLocale
import com.personal.autoscroll.domain.model.LanguageMode
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

    fun showCompact(config: OverlayConfig, languageMode: LanguageMode, actions: OverlayActions) {
        if (!canDrawOverlays() || compactOverlay != null) return

        val overlayContext = AppLocale.localizedContext(context, languageMode)
        val params = defaultParams(
            width = WindowManager.LayoutParams.WRAP_CONTENT,
            x = config.compactPositionX,
            y = config.compactPositionY,
        )
        val overlay = CompactOverlay(
            context = overlayContext,
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

    fun toggleExpanded(
        profile: com.personal.autoscroll.domain.model.AppProfile,
        languageMode: LanguageMode,
        actions: ExpandedOverlayActions,
    ) {
        if (!canDrawOverlays()) return
        if (expandedOverlay == null) {
            val overlay = ExpandedOverlay(AppLocale.localizedContext(context, languageMode), profile, actions)
            expandedOverlay = overlay
            windowManager.addView(
                overlay.view,
                defaultParams(
                    width = modalWidth(),
                    x = 0,
                    y = 0,
                    gravity = Gravity.CENTER,
                    dimAmount = 0.16f,
                ),
            )
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

    private fun defaultParams(
        width: Int,
        x: Int = 24,
        y: Int = 420,
        gravity: Int = Gravity.TOP or Gravity.START,
        dimAmount: Float = 0f,
    ): WindowManager.LayoutParams =
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
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                if (dimAmount > 0f) WindowManager.LayoutParams.FLAG_DIM_BEHIND else 0,
            android.graphics.PixelFormat.TRANSLUCENT,
        ).apply {
            this.gravity = gravity
            this.x = x
            this.y = y
            this.dimAmount = dimAmount
        }

    private fun modalWidth(): Int {
        val metrics = context.resources.displayMetrics
        return (metrics.widthPixels - context.dp(48)).coerceAtMost(context.dp(420))
    }

    private fun Context.dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
