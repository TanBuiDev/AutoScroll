package com.personal.autoscroll.core.profile

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.personal.autoscroll.core.accessibility.ForegroundAppObserver
import com.personal.autoscroll.data.repository.ProfileRepository
import com.personal.autoscroll.domain.model.AppProfile
import com.personal.autoscroll.domain.model.PresetType
import com.personal.autoscroll.domain.model.ProfileStatus
import com.personal.autoscroll.domain.model.withConfigurationUpdate
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.ConcurrentHashMap
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
    private val draftsByPackage = ConcurrentHashMap<String, AppProfile>()

    private val _foregroundPackage = MutableStateFlow<String?>(null)
    val foregroundPackage: StateFlow<String?> = _foregroundPackage.asStateFlow()

    private val _persistedProfile = MutableStateFlow<AppProfile?>(null)
    val persistedProfile: StateFlow<AppProfile?> = _persistedProfile.asStateFlow()

    private val _activeProfile = MutableStateFlow<AppProfile?>(null)
    val activeProfile: StateFlow<AppProfile?> = _activeProfile.asStateFlow()

    private val _isPersisted = MutableStateFlow(false)
    val isPersisted: StateFlow<Boolean> = _isPersisted.asStateFlow()

    private val _isDirty = MutableStateFlow(false)
    val isDirty: StateFlow<Boolean> = _isDirty.asStateFlow()

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
        val updated = current.withConfigurationUpdate(update)
        if (updated == current) return current

        draftsByPackage[current.packageName] = updated
        _activeProfile.value = updated
        updateDirtyState()
        return updated
    }

    fun markActiveProfileTested(): AppProfile? {
        val current = _activeProfile.value ?: return null
        if (current.profileStatus == ProfileStatus.Tested) return current

        val tested = current.copy(profileStatus = ProfileStatus.Tested)
        draftsByPackage[current.packageName] = tested
        _activeProfile.value = tested
        updateDirtyState()
        return tested
    }

    fun discardActiveChanges(): AppProfile? {
        val current = _activeProfile.value ?: return null
        val restored = _persistedProfile.value ?: defaultProfile(
            packageName = current.packageName,
            appName = current.appName,
            nowMillis = current.createdAt,
        )

        draftsByPackage[current.packageName] = restored
        _activeProfile.value = restored
        updateDirtyState()
        return restored
    }

    suspend fun saveActiveProfile(): AppProfile? {
        val current = _activeProfile.value ?: return null
        return saveProfile(current)
    }

    suspend fun saveProfile(profile: AppProfile): AppProfile {
        val candidate = profile.copy(updatedAt = System.currentTimeMillis())
        profileRepository.upsertProfile(candidate)
        val persisted = profileRepository.getProfile(candidate.packageName) ?: candidate

        draftsByPackage[candidate.packageName] = persisted
        if (_foregroundPackage.value == candidate.packageName) {
            _persistedProfile.value = persisted
            _activeProfile.value = persisted
            _isPersisted.value = true
            _isDirty.value = false
        }
        return persisted
    }

    suspend fun deleteProfile(packageName: String) {
        profileRepository.deleteProfile(packageName)
        draftsByPackage.remove(packageName)

        if (_foregroundPackage.value == packageName) {
            val current = _activeProfile.value
            val replacement = defaultProfile(
                packageName = packageName,
                appName = current?.appName ?: resolveAppName(packageName) ?: packageName,
            )
            draftsByPackage[packageName] = replacement
            _persistedProfile.value = null
            _activeProfile.value = replacement
            _isPersisted.value = false
            updateDirtyState()
        }
    }

    suspend fun resetProfile(packageName: String): AppProfile? {
        val existing = profileRepository.getProfile(packageName) ?: return null
        val reset = defaultProfile(
            packageName = existing.packageName,
            appName = existing.appName,
            nowMillis = existing.createdAt,
        ).copy(
            id = existing.id,
            createdAt = existing.createdAt,
            updatedAt = System.currentTimeMillis(),
        )
        profileRepository.upsertProfile(reset)
        val persisted = profileRepository.getProfile(packageName) ?: reset
        draftsByPackage[packageName] = persisted

        if (_foregroundPackage.value == packageName) {
            _persistedProfile.value = persisted
            _activeProfile.value = persisted
            _isPersisted.value = true
            _isDirty.value = false
        }
        return persisted
    }

    private var loadGeneration = 0L

    fun selectPackage(packageName: String) {
        scope.launch { loadPackage(packageName) }
    }

    private suspend fun loadPackage(packageName: String?) {
        val generation = ++loadGeneration
        _foregroundPackage.value = packageName
        if (packageName == null) {
            _persistedProfile.value = null
            _activeProfile.value = null
            _isPersisted.value = false
            _isDirty.value = false
            return
        }

        val persisted = profileRepository.getProfile(packageName)
        if (generation != loadGeneration) return
        val draft = draftsByPackage[packageName]
            ?: persisted
            ?: defaultProfile(
                packageName = packageName,
                appName = resolveAppName(packageName) ?: packageName,
            )

        draftsByPackage[packageName] = draft
        _persistedProfile.value = persisted
        _activeProfile.value = if (draft.appName == packageName) {
            draft.copy(appName = resolveAppName(packageName) ?: draft.appName)
        } else draft
        _isPersisted.value = persisted != null
        updateDirtyState()
    }

    private fun updateDirtyState() {
        _isDirty.value = _activeProfile.value != _persistedProfile.value
    }

    private fun defaultProfile(
        packageName: String,
        appName: String,
        nowMillis: Long = System.currentTimeMillis(),
    ): AppProfile = AppProfile.defaultForPackage(
        packageName = packageName,
        appName = appName,
        presetType = PresetType.VideoFeed,
        nowMillis = nowMillis,
    )

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
