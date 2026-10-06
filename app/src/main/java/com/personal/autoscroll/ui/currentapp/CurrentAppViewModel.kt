package com.personal.autoscroll.ui.currentapp

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.autoscroll.R
import com.personal.autoscroll.core.accessibility.AccessibilityServiceState
import com.personal.autoscroll.core.permissions.AccessibilityConsentStore
import com.personal.autoscroll.core.permissions.PermissionNavigator
import com.personal.autoscroll.core.permissions.PermissionState
import com.personal.autoscroll.core.overlay.OverlayAutomationCoordinator
import com.personal.autoscroll.core.profile.ActiveProfileController
import com.personal.autoscroll.domain.model.AppProfile
import com.personal.autoscroll.domain.usecase.RunTestGesture
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val profilePersisted: Boolean = false,
    val profileDirty: Boolean = false,
    val permissionState: PermissionState = PermissionState(
        accessibilityEnabled = false,
        accessibilityConnected = false,
        overlayPermissionGranted = false,
    ),
    val lastTestSucceeded: Boolean? = null,
    val accessibilityConsentAccepted: Boolean = false,
    val showAccessibilityDisclosure: Boolean = false,
    val message: CurrentAppMessage? = null,
)

data class CurrentAppMessage(
    @param:StringRes val resId: Int,
    val arg: String? = null,
)

private data class ActiveProfileSnapshot(
    val packageName: String?,
    val profile: AppProfile?,
    val persisted: Boolean,
    val dirty: Boolean,
)

@HiltViewModel
class CurrentAppViewModel @Inject constructor(
    private val activeProfileController: ActiveProfileController,
    private val permissionNavigator: PermissionNavigator,
    private val runTestGesture: RunTestGesture,
    private val accessibilityConsentStore: AccessibilityConsentStore,
    private val overlayAutomationCoordinator: OverlayAutomationCoordinator,
) : ViewModel() {
    val automationState = overlayAutomationCoordinator.automationState
    private val manualState = MutableStateFlow(CurrentAppUiState())
    private val permissionRefresh = MutableStateFlow(0)

    private val activeProfileSnapshot = combine(
        activeProfileController.foregroundPackage,
        activeProfileController.activeProfile,
        activeProfileController.isPersisted,
        activeProfileController.isDirty,
    ) { packageName, profile, persisted, dirty ->
        ActiveProfileSnapshot(
            packageName = packageName,
            profile = profile,
            persisted = persisted,
            dirty = dirty,
        )
    }

    val uiState: StateFlow<CurrentAppUiState> = combine(
        activeProfileSnapshot,
        AccessibilityServiceState.isConnected,
        permissionRefresh,
        manualState,
        accessibilityConsentStore.accepted,
    ) { active, serviceConnected, _, manual, consentAccepted ->
        val accessibilityEnabled = permissionNavigator.isAccessibilityEnabled()
        manual.copy(
            foregroundPackage = active.packageName,
            foregroundAppName = active.profile?.appName,
            profile = active.profile,
            profilePersisted = active.persisted,
            profileDirty = active.dirty,
            accessibilityConsentAccepted = consentAccepted,
            permissionState = PermissionState(
                accessibilityEnabled = accessibilityEnabled,
                accessibilityConnected = serviceConnected,
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
        if (!uiState.value.accessibilityConsentAccepted) {
            showAccessibilityDisclosure()
            return
        }
        val profile = uiState.value.profile ?: return
        viewModelScope.launch {
            val succeeded = runTestGesture(profile)
            if (
                succeeded &&
                activeProfileController.activeProfile.value?.packageName == profile.packageName
            ) {
                activeProfileController.markActiveProfileTested()
            }
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
        if (uiState.value.profile == null) return
        viewModelScope.launch {
            val saved = activeProfileController.saveActiveProfile() ?: return@launch
            manualState.update {
                it.copy(
                    message = CurrentAppMessage(
                        R.string.message_profile_saved,
                        saved.appName,
                    ),
                )
            }
        }
    }

    fun requestAccessibilityAccess() {
        if (uiState.value.accessibilityConsentAccepted) {
            permissionNavigator.openAccessibilitySettings()
        } else {
            showAccessibilityDisclosure()
        }
    }

    fun showAccessibilityDisclosure() {
        manualState.update { it.copy(showAccessibilityDisclosure = true) }
    }

    fun dismissAccessibilityDisclosure() {
        manualState.update { it.copy(showAccessibilityDisclosure = false) }
    }

    fun acceptAccessibilityDisclosure() {
        accessibilityConsentStore.accept()
        manualState.update { it.copy(showAccessibilityDisclosure = false) }
        permissionNavigator.openAccessibilitySettings()
    }

    fun openPrivacyPolicy() {
        permissionNavigator.openPrivacyPolicy()
    }

    fun openAppSettings() = permissionNavigator.openAppSettings()

    fun openOverlaySettings() {
        permissionNavigator.openOverlaySettings()
    }
}
