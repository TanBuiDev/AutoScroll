package com.personal.autoscroll.core.profile

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.personal.autoscroll.core.accessibility.ForegroundAppObserver
import com.personal.autoscroll.data.repository.ProfileRepository
import com.personal.autoscroll.domain.model.AppProfile
import com.personal.autoscroll.domain.model.PresetType
import com.personal.autoscroll.domain.model.ProfileStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Singleton
class ActiveProfileController @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val foregroundAppObserver: ForegroundAppObserver,
    private val profileRepository: ProfileRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _foregroundPackage = MutableStateFlow<String?>(null)
    val foregroundPackage: StateFlow<String?> = _foregroundPackage.asStateFlow()

    private val _activeProfile = MutableStateFlow<AppProfile?>(null)
    val activeProfile: StateFlow<AppProfile?> = _activeProfile.asStateFlow()

    private val _isPersisted = MutableStateFlow(false)
    val isPersisted: StateFlow<Boolean> = _isPersisted.asStateFlow()

    init {
        scope.launch {
            foregroundAppObserver
                .foregroundPackageExcluding(context.packageName)
                .distinctUntilChanged()
                .collectLatest(::loadPackage)
        }
    }

    fun updateActiveProfile(update: AppProfile.() -> AppProfile): AppProfile? {
        val current = _activeProfile.value ?: return null
        val updated = current.update()
        _activeProfile.value = updated
        return updated
    }

    suspend fun saveActiveProfile(
        profileStatus: ProfileStatus? = null,
    ): AppProfile? {
        val current = _activeProfile.value ?: return null
        val saved = current.copy(
            profileStatus = profileStatus ?: current.profileStatus,
            updatedAt = System.currentTimeMillis(),
        )
        profileRepository.upsertProfile(saved)
        _activeProfile.value = saved
        _isPersisted.value = true
        return saved
    }

    private suspend fun loadPackage(packageName: String?) {
        _foregroundPackage.value = packageName
        if (packageName == null) {
            _activeProfile.value = null
            _isPersisted.value = false
            return
        }

        val persisted = profileRepository.getProfile(packageName)
        _isPersisted.value = persisted != null
        _activeProfile.value = persisted ?: AppProfile.defaultForPackage(
            packageName = packageName,
            appName = resolveAppName(packageName) ?: packageName,
            presetType = PresetType.VideoFeed,
        )
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
