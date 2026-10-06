package com.personal.autoscroll.ui.automation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.autoscroll.core.profile.ActiveProfileController
import com.personal.autoscroll.domain.model.AppProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AutomationUiState(
    val profile: AppProfile? = null,
    val isPersisted: Boolean = false,
    val isDirty: Boolean = false,
    val isSaving: Boolean = false,
    val saveFailed: Boolean = false,
)

@HiltViewModel
class AutomationViewModel @Inject constructor(
    private val activeProfileController: ActiveProfileController,
    @param:dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
) : ViewModel() {
    private val saving = kotlinx.coroutines.flow.MutableStateFlow(false)
    private val saveFailed = kotlinx.coroutines.flow.MutableStateFlow(false)
    val uiState: StateFlow<AutomationUiState> = combine(
        activeProfileController.activeProfile,
        activeProfileController.isPersisted,
        activeProfileController.isDirty,
        saving,
        saveFailed,
    ) { profile, isPersisted, isDirty, isSaving, failed ->
        AutomationUiState(
            profile = profile,
            isPersisted = isPersisted,
            isDirty = isDirty,
            isSaving = isSaving,
            saveFailed = failed,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AutomationUiState(),
    )

    val installedApps = kotlinx.coroutines.flow.MutableStateFlow<List<SelectableApp>>(emptyList())
    val appsLoading = kotlinx.coroutines.flow.MutableStateFlow(false)
    val serviceConnected = com.personal.autoscroll.core.accessibility.AccessibilityServiceState.isConnected

    fun loadInstalledApps() {
        viewModelScope.launch {
            appsLoading.value = true
            try {
                installedApps.value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val intent = android.content.Intent(android.content.Intent.ACTION_MAIN).addCategory(android.content.Intent.CATEGORY_LAUNCHER)
                    context.packageManager.queryIntentActivities(intent, 0)
                        .filter { it.activityInfo.packageName != context.packageName }
                        .map { SelectableApp(it.activityInfo.packageName, it.loadLabel(context.packageManager).toString()) }
                        .distinctBy { it.packageName }.sortedBy { it.name.lowercase() }
                }
            } finally { appsLoading.value = false }
        }
    }

    fun selectApp(app: SelectableApp) { activeProfileController.selectPackage(app.packageName) }

    fun updateProfile(transform: AppProfile.() -> AppProfile) {
        activeProfileController.updateActiveProfile(transform)
    }

    fun save() {
        if (saving.value) return
        saving.value = true
        saveFailed.value = false
        viewModelScope.launch {
            try { activeProfileController.saveActiveProfile() }
            catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch (_: Exception) { saveFailed.value = true }
            finally { saving.value = false }
        }
    }

    fun discard() {
        activeProfileController.discardActiveChanges()
    }
}

data class SelectableApp(val packageName: String, val name: String)
