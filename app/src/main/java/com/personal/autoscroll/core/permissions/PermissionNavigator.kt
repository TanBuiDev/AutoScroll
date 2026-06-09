package com.personal.autoscroll.core.permissions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.personal.autoscroll.core.accessibility.AutoScrollAccessibilityService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PermissionNavigator @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    fun openAccessibilitySettings() {
        context.startActivity(
            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    fun openOverlaySettings() {
        context.startActivity(
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}"),
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    fun isAccessibilityEnabled(): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false
        val serviceName = "${context.packageName}/${AutoScrollAccessibilityService::class.java.name}"
        val shortServiceName = "${context.packageName}/.core.accessibility.AutoScrollAccessibilityService"
        return enabledServices.contains(serviceName, ignoreCase = true) ||
            enabledServices.contains(shortServiceName, ignoreCase = true)
    }

    fun canDrawOverlays(): Boolean = Settings.canDrawOverlays(context)
}
