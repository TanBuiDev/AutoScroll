package com.personal.autoscroll.ui.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.autoscroll.data.repository.ProfileRepository
import com.personal.autoscroll.domain.model.AppProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ProfilesViewModel @Inject constructor(
    profileRepository: ProfileRepository,
) : ViewModel() {
    val profiles: StateFlow<List<AppProfile>> = profileRepository.observeAllProfiles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )
}
