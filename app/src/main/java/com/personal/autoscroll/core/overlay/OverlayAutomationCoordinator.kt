package com.personal.autoscroll.core.overlay

import com.personal.autoscroll.core.automation.AutomationController
import com.personal.autoscroll.core.automation.AutomationState
import com.personal.autoscroll.core.gesture.GestureExecutor
import com.personal.autoscroll.core.gesture.GestureMapper
import com.personal.autoscroll.core.profile.ActiveProfileController
import com.personal.autoscroll.data.datastore.SettingsDataStore
import com.personal.autoscroll.domain.model.AppProfile
import com.personal.autoscroll.domain.model.GestureAxis
import com.personal.autoscroll.domain.model.GlobalSettings
import com.personal.autoscroll.domain.model.IntentDirection
import com.personal.autoscroll.domain.model.LanguageMode
import com.personal.autoscroll.domain.model.PresetType
import com.personal.autoscroll.domain.model.ScrollMode
import com.personal.autoscroll.domain.model.applyGlobalSettings
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Singleton
class OverlayAutomationCoordinator @Inject constructor(
    private val overlayController: OverlayController,
    private val automationController: AutomationController,
    private val gestureExecutor: GestureExecutor,
    private val gestureMapper: GestureMapper,
    private val activeProfileController: ActiveProfileController,
    private val settingsDataStore: SettingsDataStore,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var manualProfile: AppProfile = AppProfile.defaultForPackage(
        packageName = MANUAL_PACKAGE,
        appName = "Manual Overlay",
        presetType = PresetType.VideoFeed,
    )
    private var stateJob: Job? = null
    private var appChangeJob: Job? = null
    private var languageMode: LanguageMode = LanguageMode.System
    private var globalSettings: GlobalSettings = GlobalSettings.Default

    init {
        scope.launch {
            activeProfileController.activeProfile.collectLatest {
                refreshOverlays(currentEffectiveProfile())
            }
        }
        scope.launch {
            settingsDataStore.settings.collectLatest(::applyGlobalSettings)
        }
    }

    fun showOverlay(languageMode: LanguageMode) {
        this.languageMode = languageMode
        val profile = currentEffectiveProfile()

        overlayController.showCompact(
            config = profile.overlayConfig,
            gestureAxis = profile.gestureConfig.axis,
            languageMode = languageMode,
            actions = OverlayActions(
                onStartStop = { toggleAutomation() },
                onNext = { runManualGesture(IntentDirection.NextItem) },
                onPrevious = { runManualGesture(IntentDirection.PreviousItem) },
                onSettings = { toggleSettings() },
                onClose = { hideOverlay() },
            ),
            onPositionChanged = { x, y ->
                scope.launch {
                    settingsDataStore.updateCompactPosition(x, y)
                }
            },
        )

        stateJob?.cancel()
        stateJob = scope.launch {
            automationController.state.collectLatest { state ->
                overlayController.setRunning(state is AutomationState.Running)
            }
        }
    }

    fun hideOverlay() {
        automationController.stop()
        appChangeJob?.cancel()
        appChangeJob = null
        stateJob?.cancel()
        stateJob = null
        overlayController.hideAll()
    }

    fun applyGlobalSettings(settings: GlobalSettings) {
        globalSettings = settings
        refreshOverlays(currentEffectiveProfile())
    }

    fun updateGestureAxis(axis: GestureAxis) {
        updateProfile {
            copy(gestureConfig = gestureConfig.copy(axis = axis))
        }
    }

    fun updateIntentDirection(direction: IntentDirection) {
        updateProfile {
            copy(gestureConfig = gestureConfig.copy(intentDirection = direction))
        }
    }

    fun updateGestureDistance(distancePercent: Int) {
        updateProfile {
            copy(
                gestureConfig = gestureConfig.copy(
                    distancePercent = distancePercent.coerceIn(5, 95),
                ),
            )
        }
    }

    fun updateSwipeDuration(durationMillis: Long) {
        updateProfile {
            copy(
                gestureConfig = gestureConfig.copy(
                    swipeDurationMillis = durationMillis.coerceIn(200L, 2_000L),
                ),
            )
        }
    }

    fun updateScrollMode(mode: ScrollMode) {
        updateProfile {
            copy(
                timingConfig = timingConfig.copy(
                    mode = mode,
                    repeatCount = if (mode == ScrollMode.Repeat) {
                        timingConfig.repeatCount ?: 1
                    } else {
                        timingConfig.repeatCount
                    },
                    durationMillis = if (mode == ScrollMode.Timer) {
                        timingConfig.durationMillis ?: 30L * 60_000L
                    } else {
                        timingConfig.durationMillis
                    },
                ),
            )
        }
    }

    fun updateDelayMillis(delayMillis: Long) {
        updateProfile {
            copy(
                timingConfig = timingConfig.copy(
                    delayMillis = delayMillis.coerceAtLeast(500L),
                ),
            )
        }
    }

    fun updateRepeatCount(repeatCount: Int) {
        updateProfile {
            copy(
                timingConfig = timingConfig.copy(
                    repeatCount = repeatCount.coerceAtLeast(1),
                ),
            )
        }
    }

    fun updateDurationMillis(durationMillis: Long) {
        updateProfile {
            copy(
                timingConfig = timingConfig.copy(
                    durationMillis = durationMillis.coerceAtLeast(1_000L),
                ),
            )
        }
    }

    private fun toggleAutomation() {
        if (automationController.isRunning) {
            automationController.stop()
            appChangeJob?.cancel()
            appChangeJob = null
            overlayController.setRunning(false)
            return
        }

        val profile = currentEffectiveProfile()
        automationController.start(
            scope = scope,
            timingConfig = profile.timingConfig,
            gestureConfigProvider = { currentEffectiveProfile().gestureConfig },
        )
        startAppChangeWatch(profile)
        overlayController.setRunning(true)
        if (profile.overlayConfig.autoCollapse) {
            overlayController.collapseExpanded()
        }
    }

    private fun startAppChangeWatch(profile: AppProfile) {
        appChangeJob?.cancel()
        appChangeJob = null
        if (!profile.timingConfig.stopOnAppChange || profile.packageName == MANUAL_PACKAGE) {
            return
        }

        val targetPackage = profile.packageName
        appChangeJob = scope.launch {
            activeProfileController.foregroundPackage.collectLatest { foregroundPackage ->
                if (
                    automationController.isRunning &&
                    foregroundPackage != null &&
                    foregroundPackage != targetPackage
                ) {
                    automationController.stop()
                    overlayController.setRunning(false)
                    appChangeJob = null
                    cancel()
                }
            }
        }
    }

    private fun runManualGesture(direction: IntentDirection) {
        scope.launch {
            val gestureConfig = currentEffectiveProfile().gestureConfig.copy(
                intentDirection = direction,
            )
            gestureExecutor.execute(gestureConfig)
        }
    }

    private fun toggleSettings() {
        overlayController.toggleExpanded(
            profile = currentEffectiveProfile(),
            languageMode = languageMode,
            actions = ExpandedOverlayActions(
                onModeChanged = ::updateScrollMode,
                onDirectionChanged = ::updateIntentDirection,
                onDelayChanged = ::updateDelayMillis,
                onSwipeDurationChanged = ::updateSwipeDuration,
                onOverlayOrientationChanged = { orientation ->
                    updateProfile {
                        copy(overlayConfig = overlayConfig.copy(orientation = orientation))
                    }
                },
                onOpacityChanged = { opacity ->
                    scope.launch {
                        settingsDataStore.updateOverlayOpacity(opacity)
                    }
                },
                onRepeatCountChanged = ::updateRepeatCount,
                onDurationChanged = ::updateDurationMillis,
                onPreview = {
                    overlayController.showGesturePreview(
                        plan = gestureMapper.map(currentEffectiveProfile().gestureConfig),
                        languageMode = languageMode,
                    )
                },
                onSave = {
                    scope.launch {
                        activeProfileController.saveActiveProfile()
                    }
                    overlayController.collapseExpanded()
                },
            ),
        )
    }

    private fun updateProfile(transform: AppProfile.() -> AppProfile) {
        val activeProfile = activeProfileController.activeProfile.value
        val baseProfile = activeProfile ?: manualProfile
        val updated = normalizeProfile(baseProfile.transform())

        if (activeProfile != null) {
            activeProfileController.updateActiveProfile { updated }
        } else {
            manualProfile = updated
        }

        refreshOverlays(currentEffectiveProfile())
    }

    private fun normalizeProfile(profile: AppProfile): AppProfile {
        var normalized = profile
        if (normalized.timingConfig.mode != ScrollMode.Repeat) {
            normalized = normalized.copy(
                timingConfig = normalized.timingConfig.copy(repeatCount = null),
            )
        }
        if (normalized.timingConfig.mode != ScrollMode.Timer) {
            normalized = normalized.copy(
                timingConfig = normalized.timingConfig.copy(durationMillis = null),
            )
        }
        return normalized.copy(
            overlayConfig = normalized.overlayConfig.copy(
                opacity = normalized.overlayConfig.opacity.coerceIn(0.3f, 1f),
            ),
        )
    }

    private fun currentEffectiveProfile(): AppProfile {
        val profile = activeProfileController.activeProfile.value ?: manualProfile
        return profile.copy(
            overlayConfig = profile.overlayConfig.applyGlobalSettings(globalSettings),
        )
    }

    private fun refreshOverlays(profile: AppProfile) {
        overlayController.updateCompact(profile.overlayConfig)
        overlayController.updateGestureAxis(profile.gestureConfig.axis)
        overlayController.updateExpanded(profile)
    }

    private companion object {
        const val MANUAL_PACKAGE = "manual.overlay"
    }
}
