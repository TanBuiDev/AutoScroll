package com.personal.autoscroll.ui.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.autoscroll.core.profile.ActiveProfileController
import com.personal.autoscroll.data.repository.ProfileRepository
import com.personal.autoscroll.domain.model.AppProfile
import com.personal.autoscroll.domain.model.withConfigurationUpdate
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileEditState(
    val original: AppProfile? = null,
    val draft: AppProfile? = null,
) {
    val isDirty: Boolean
        get() = draft != null && draft != original
}

@HiltViewModel
class ProfilesViewModel @Inject constructor(
    profileRepository: ProfileRepository,
    private val activeProfileController: ActiveProfileController,
) : ViewModel() {
    val profiles: StateFlow<List<AppProfile>> = profileRepository.observeAllProfiles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    private val _editState = MutableStateFlow(ProfileEditState())
    val editState: StateFlow<ProfileEditState> = _editState.asStateFlow()

    fun beginEdit(profile: AppProfile) {
        _editState.value = ProfileEditState(
            original = profile,
            draft = profile,
        )
    }

    fun cancelEdit() {
        _editState.value = ProfileEditState()
    }

    fun updateEditingProfile(transform: AppProfile.() -> AppProfile) {
        val current = _editState.value.draft ?: return
        _editState.value = _editState.value.copy(
            draft = current.withConfigurationUpdate(transform),
        )
    }

    fun saveEditingProfile() {
        val draft = _editState.value.draft ?: return
        viewModelScope.launch {
            activeProfileController.saveProfile(draft)
            _editState.value = ProfileEditState()
        }
    }

    fun deleteProfile(profile: AppProfile) {
        viewModelScope.launch {
            activeProfileController.deleteProfile(profile.packageName)
            if (_editState.value.draft?.packageName == profile.packageName) {
                _editState.value = ProfileEditState()
            }
        }
    }

    fun resetProfile(profile: AppProfile) {
        viewModelScope.launch {
            activeProfileController.resetProfile(profile.packageName)
            if (_editState.value.draft?.packageName == profile.packageName) {
                _editState.value = ProfileEditState()
            }
        }
    }
}
