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
)

@HiltViewModel
class AutomationViewModel @Inject constructor(
    private val activeProfileController: ActiveProfileController,
) : ViewModel() {
    val uiState: StateFlow<AutomationUiState> = combine(
        activeProfileController.activeProfile,
        activeProfileController.isPersisted,
        activeProfileController.isDirty,
    ) { profile, isPersisted, isDirty ->
        AutomationUiState(
            profile = profile,
            isPersisted = isPersisted,
            isDirty = isDirty,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AutomationUiState(),
    )

    fun updateProfile(transform: AppProfile.() -> AppProfile) {
        activeProfileController.updateActiveProfile(transform)
    }

    fun save() {
        viewModelScope.launch {
            activeProfileController.saveActiveProfile()
        }
    }

    fun discard() {
        activeProfileController.discardActiveChanges()
    }
}
