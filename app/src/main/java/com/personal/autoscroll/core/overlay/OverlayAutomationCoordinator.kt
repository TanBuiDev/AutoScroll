package com.personal.autoscroll.core.overlay

import com.personal.autoscroll.core.accessibility.AutoScrollAccessibilityService
import com.personal.autoscroll.core.automation.AutomationController
import com.personal.autoscroll.core.automation.AutomationState
import com.personal.autoscroll.core.gesture.GestureMapper
import com.personal.autoscroll.domain.model.AppProfile
import com.personal.autoscroll.domain.model.IntentDirection
import com.personal.autoscroll.domain.model.LanguageMode
import com.personal.autoscroll.domain.model.PresetType
import com.personal.autoscroll.domain.model.ScrollMode
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OverlayAutomationCoordinator @Inject constructor(
    private val overlayController: OverlayController,
    private val automationController: AutomationController,
    private val gestureMapper: GestureMapper,
) {
    private var profile: AppProfile = AppProfile.defaultForPackage(
        packageName = "manual.overlay",
        appName = "Manual Overlay",
        presetType = PresetType.VideoFeed,
    )
    private var stateJob: Job? = null
    private var languageMode: LanguageMode = LanguageMode.System

    fun showOverlay(scope: CoroutineScope, languageMode: LanguageMode) {
        this.languageMode = languageMode
        overlayController.showCompact(
            config = profile.overlayConfig,
            languageMode = languageMode,
            OverlayActions(
                onStartStop = { toggleAutomation(scope) },
                onNext = { runManualGesture(scope, IntentDirection.NextItem) },
                onPrevious = { runManualGesture(scope, IntentDirection.PreviousItem) },
                onSettings = { toggleSettings(scope) },
            ),
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
        stateJob?.cancel()
        stateJob = null
        overlayController.hideAll()
    }

    private fun toggleAutomation(scope: CoroutineScope) {
        if (automationController.isRunning) {
            automationController.stop()
            overlayController.setRunning(false)
            return
        }

        automationController.start(
            scope = scope,
            timingConfig = profile.timingConfig,
        ) {
            dispatchGesture(profile)
        }
        overlayController.setRunning(true)
        overlayController.collapseExpanded()
    }

    private fun runManualGesture(scope: CoroutineScope, direction: IntentDirection) {
        scope.launch {
            dispatchGesture(
                profile.copy(
                    gestureConfig = profile.gestureConfig.copy(
                        intentDirection = direction,
                    ),
                ),
            )
        }
    }

    private suspend fun dispatchGesture(profile: AppProfile): Boolean {
        val dispatcher = AutoScrollAccessibilityService.dispatcherOrNull() ?: return false
        return dispatcher.dispatch(gestureMapper.map(profile.gestureConfig))
    }

    private fun toggleSettings(scope: CoroutineScope) {
        overlayController.toggleExpanded(
            profile = profile,
            languageMode = languageMode,
            actions = ExpandedOverlayActions(
                onModeChanged = { mode ->
                    updateProfile { copy(timingConfig = timingConfig.copy(mode = mode)) }
                },
                onDirectionChanged = { direction ->
                    updateProfile { copy(gestureConfig = gestureConfig.copy(intentDirection = direction)) }
                },
                onDelayChanged = { delay ->
                    updateProfile { copy(timingConfig = timingConfig.copy(delayMillis = delay)) }
                },
                onSwipeDurationChanged = { swipeDurationMillis ->
                    updateProfile {
                        copy(gestureConfig = gestureConfig.copy(swipeDurationMillis = swipeDurationMillis))
                    }
                },
                onOverlayOrientationChanged = { orientation ->
                    updateProfile {
                        copy(overlayConfig = overlayConfig.copy(orientation = orientation))
                    }
                },
                onOpacityChanged = { opacity ->
                    updateProfile {
                        copy(overlayConfig = overlayConfig.copy(opacity = opacity))
                    }
                },
                onRepeatCountChanged = { repeatCount ->
                    updateProfile { copy(timingConfig = timingConfig.copy(repeatCount = repeatCount)) }
                },
                onDurationChanged = { duration ->
                    updateProfile { copy(timingConfig = timingConfig.copy(durationMillis = duration)) }
                },
                onSave = {
                    overlayController.updateExpanded(profile)
                    overlayController.collapseExpanded()
                },
            ),
        )
    }

    private fun updateProfile(update: AppProfile.() -> AppProfile) {
        profile = profile.update()
        if (profile.timingConfig.mode != ScrollMode.Repeat) {
            profile = profile.copy(timingConfig = profile.timingConfig.copy(repeatCount = null))
        }
        if (profile.timingConfig.mode != ScrollMode.Timer) {
            profile = profile.copy(timingConfig = profile.timingConfig.copy(durationMillis = null))
        }
        profile = profile.copy(
            overlayConfig = profile.overlayConfig.copy(
                opacity = profile.overlayConfig.opacity.coerceIn(0.3f, 1f),
            ),
        )
        overlayController.updateCompact(profile.overlayConfig)
        overlayController.updateExpanded(profile)
    }
}
