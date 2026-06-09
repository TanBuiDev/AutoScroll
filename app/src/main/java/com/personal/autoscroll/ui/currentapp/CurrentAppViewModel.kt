package com.personal.autoscroll.ui.currentapp

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.autoscroll.R
import com.personal.autoscroll.core.accessibility.AccessibilityServiceState
import com.personal.autoscroll.core.accessibility.AutoScrollAccessibilityService
import com.personal.autoscroll.core.accessibility.ForegroundAppObserver
import com.personal.autoscroll.core.permissions.PermissionNavigator
import com.personal.autoscroll.core.permissions.PermissionState
import com.personal.autoscroll.domain.model.AppProfile
import com.personal.autoscroll.domain.model.PresetType
import com.personal.autoscroll.domain.model.ProfileStatus
import com.personal.autoscroll.domain.usecase.LoadProfileForPackage
import com.personal.autoscroll.domain.usecase.RunTestGesture
import com.personal.autoscroll.domain.usecase.SaveCurrentAppProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CurrentAppUiState(
    val foregroundPackage: String? = null,
    val foregroundAppName: String? = null,
    val profile: AppProfile? = null,
    val permissionState: PermissionState = PermissionState(
        accessibilityEnabled = false,
        accessibilityConnected = false,
        overlayPermissionGranted = false,
    ),
    val lastTestSucceeded: Boolean? = null,
    val message: CurrentAppMessage? = null,
)

data class CurrentAppMessage(
    @param:StringRes val resId: Int,
    val arg: String? = null,
)

@HiltViewModel
class CurrentAppViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    foregroundAppObserver: ForegroundAppObserver,
    private val permissionNavigator: PermissionNavigator,
    private val loadProfileForPackage: LoadProfileForPackage,
    private val saveCurrentAppProfile: SaveCurrentAppProfile,
    private val runTestGesture: RunTestGesture,
) : ViewModel() {
    private val manualState = MutableStateFlow(CurrentAppUiState())
    private val permissionRefresh = MutableStateFlow(0)

    val uiState: StateFlow<CurrentAppUiState> = combine(
        foregroundAppObserver.foregroundPackageExcluding(context.packageName),
        AccessibilityServiceState.isConnected,
        permissionRefresh,
        manualState,
    ) { packageName, serviceConnected, _, manual ->
        val appName = packageName?.let { resolveAppName(it) }
        val profile = packageName?.let {
            loadProfileForPackage(
                packageName = it,
                appName = appName ?: it,
                presetType = PresetType.VideoFeed,
            )
        }
        val accessibilityEnabled = permissionNavigator.isAccessibilityEnabled()
        manual.copy(
            foregroundPackage = packageName,
            foregroundAppName = appName,
            profile = profile,
            permissionState = PermissionState(
                accessibilityEnabled = accessibilityEnabled,
                accessibilityConnected = serviceConnected || accessibilityEnabled,
                overlayPermissionGranted = permissionNavigator.canDrawOverlays(),
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CurrentAppUiState(),
    )

    fun refreshPermissions() {
        permissionRefresh.update { it + 1 }
    }

    fun testGesture() {
        val profile = uiState.value.profile ?: return
        viewModelScope.launch {
            val succeeded = runTestGesture(
                profile = profile,
                dispatcher = AutoScrollAccessibilityService.dispatcherOrNull(),
            )
            manualState.update {
                it.copy(
                    lastTestSucceeded = succeeded,
                    message = if (succeeded) {
                        CurrentAppMessage(R.string.message_test_completed)
                    } else {
                        CurrentAppMessage(R.string.message_test_failed)
                    },
                )
            }
        }
    }

    fun saveProfile() {
        val profile = uiState.value.profile ?: return
        viewModelScope.launch {
            saveCurrentAppProfile(
                profile.copy(profileStatus = ProfileStatus.Tested),
            )
            manualState.update {
                it.copy(message = CurrentAppMessage(R.string.message_profile_saved, profile.appName))
            }
        }
    }

    fun openAccessibilitySettings() {
        permissionNavigator.openAccessibilitySettings()
    }

    fun openOverlaySettings() {
        permissionNavigator.openOverlaySettings()
    }

    private fun resolveAppName(packageName: String): String? =
        runCatching {
            val applicationInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.ApplicationInfoFlags.of(0),
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getApplicationInfo(packageName, 0)
            }
            context.packageManager.getApplicationLabel(applicationInfo).toString()
        }.getOrNull()
}
