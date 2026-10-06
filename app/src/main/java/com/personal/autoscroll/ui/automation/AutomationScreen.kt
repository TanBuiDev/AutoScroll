package com.personal.autoscroll.ui.automation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personal.autoscroll.ui.foundation.*
import com.personal.autoscroll.R
import com.personal.autoscroll.domain.model.AppProfile
import com.personal.autoscroll.domain.model.PresetType
import com.personal.autoscroll.domain.model.ScrollMode
import com.personal.autoscroll.domain.model.withPreset
import com.personal.autoscroll.ui.gesture.GestureScreen
import com.personal.autoscroll.ui.timing.TimingScreen

@Composable
fun AutomationScreen(viewModel: AutomationViewModel, onOpenCurrentApp: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val connected by viewModel.serviceConnected.collectAsStateWithLifecycle()
    var chooseApp by remember { mutableStateOf(false) }
    var confirmDiscard by remember { mutableStateOf(false) }
    if (confirmDiscard) UnsavedChangesDialog(onKeepEditing = { confirmDiscard = false }, onDiscard = { viewModel.discard(); confirmDiscard = false })
    var pendingApp by remember { mutableStateOf<SelectableApp?>(null) }
    if (chooseApp) AppChooserDialog(viewModel, onDismiss = { chooseApp = false }, onSelect = { app ->
        if (state.isDirty) pendingApp = app else { viewModel.selectApp(app); chooseApp = false }
    })
    pendingApp?.let { app ->
        UnsavedChangesDialog(onKeepEditing = { pendingApp = null }, onDiscard = {
            viewModel.discard(); viewModel.selectApp(app); pendingApp = null; chooseApp = false
        })
    }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.nav_automation),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Text(stringResource(R.string.profile_usage_help), style = MaterialTheme.typography.bodySmall)
        OutlinedButton(onClick = { viewModel.loadInstalledApps(); chooseApp = true }) {
            Text(stringResource(R.string.choose_app))
        }
        if (!connected) {
            Text(stringResource(R.string.accessibility_configuration_help), color = MaterialTheme.colorScheme.error)
            OutlinedButton(onClick = onOpenCurrentApp) { Text(stringResource(R.string.action_check_permissions)) }
        }
        val profile = state.profile
        if (profile == null) {
            Text(
                text = stringResource(R.string.automation_no_active_profile),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@Column
        }

        AppIdentity(profile)
        AppPackageDetails(profile.packageName)
        Text(stringResource(R.string.profile_name_explanation), style = MaterialTheme.typography.bodySmall)
        ProfileEditorContent(
            profile = profile,
            onProfileChange = viewModel::updateProfile,
        )

      }
        if (state.saveFailed) Text(stringResource(R.string.save_error), color = MaterialTheme.colorScheme.error)
        if (state.isDirty) {
            Text(
                text = if (state.isPersisted) {
                    stringResource(R.string.profile_unsaved_changes)
                } else {
                    stringResource(R.string.profile_not_saved)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Button(
                onClick = viewModel::save,
                enabled = state.isDirty && !state.isSaving,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(if (state.isSaving) R.string.action_saving else R.string.action_save_changes))
            }
            OutlinedButton(
                onClick = { confirmDiscard = true },
                enabled = state.isDirty && !state.isSaving,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.action_discard))
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ProfileEditorContent(
    profile: AppProfile,
    onProfileChange: (AppProfile.() -> AppProfile) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.gesture_test_explanation), style = MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Switch(
                checked = profile.enabled,
                onCheckedChange = { enabled ->
                    onProfileChange { copy(enabled = enabled) }
                },
            )
            Column {
                Text(
                    text = stringResource(R.string.profile_enabled),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(profile.profileStatus.labelRes()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Text(
            text = stringResource(R.string.label_preset),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PresetType.entries.forEach { preset ->
                FilterChip(
                    selected = preset == profile.presetType,
                    onClick = { onProfileChange { withPreset(preset) } },
                    label = { Text(stringResource(preset.labelRes()), maxLines = 2) },
                )
            }
        }

        GestureScreen(
            config = profile.gestureConfig,
            onGestureAxisChanged = { axis ->
                onProfileChange {
                    copy(gestureConfig = gestureConfig.copy(axis = axis))
                }
            },
            onIntentDirectionChanged = { direction ->
                onProfileChange {
                    copy(gestureConfig = gestureConfig.copy(intentDirection = direction))
                }
            },
            onGestureDistanceChanged = { distance ->
                onProfileChange {
                    copy(
                        gestureConfig = gestureConfig.copy(
                            distancePercent = distance.coerceIn(5, 95),
                        ),
                    )
                }
            },
            onSwipeDurationChanged = { duration ->
                onProfileChange {
                    copy(
                        gestureConfig = gestureConfig.copy(
                            swipeDurationMillis = duration.coerceIn(200L, 2_000L),
                        ),
                    )
                }
            },
            onStartXChanged = { startX ->
                onProfileChange {
                    copy(
                        gestureConfig = gestureConfig.copy(
                            startXPercent = startX.coerceIn(0, 100),
                        ),
                    )
                }
            },
            onStartYChanged = { startY ->
                onProfileChange {
                    copy(
                        gestureConfig = gestureConfig.copy(
                            startYPercent = startY.coerceIn(0, 100),
                        ),
                    )
                }
            },
            onInvertPhysicalDirectionChanged = { inverted ->
                onProfileChange {
                    copy(
                        gestureConfig = gestureConfig.copy(
                            invertPhysicalDirection = inverted,
                        ),
                    )
                }
            },
        )

        TimingScreen(
            config = profile.timingConfig,
            onModeChanged = { mode ->
                onProfileChange {
                    copy(
                        timingConfig = timingConfig.copy(
                            mode = mode,
                            repeatCount = if (mode == ScrollMode.Repeat) {
                                timingConfig.repeatCount ?: 1
                            } else {
                                null
                            },
                            durationMillis = if (mode == ScrollMode.Timer) {
                                timingConfig.durationMillis ?: 30L * 60_000L
                            } else {
                                null
                            },
                        ),
                    )
                }
            },
            onDelayChanged = { delay ->
                onProfileChange {
                    copy(
                        timingConfig = timingConfig.copy(
                            delayMillis = delay.coerceIn(500L, 60_000L),
                        ),
                    )
                }
            },
            onStartDelayChanged = { startDelay ->
                onProfileChange {
                    copy(
                        timingConfig = timingConfig.copy(
                            startDelayMillis = startDelay.coerceIn(0L, 30_000L),
                        ),
                    )
                }
            },
            onRepeatCountChanged = { repeatCount ->
                onProfileChange {
                    copy(
                        timingConfig = timingConfig.copy(
                            repeatCount = repeatCount.coerceAtLeast(1),
                        ),
                    )
                }
            },
            onDurationChanged = { duration ->
                onProfileChange {
                    copy(
                        timingConfig = timingConfig.copy(
                            durationMillis = duration.coerceAtLeast(60_000L),
                        ),
                    )
                }
            },
            onStopOnAppChangeChanged = { stopOnAppChange ->
                onProfileChange {
                    copy(
                        timingConfig = timingConfig.copy(
                            stopOnAppChange = stopOnAppChange,
                        ),
                    )
                }
            },
        )
    }
}

@Composable
private fun PresetType.label(): String = when (this) {
    PresetType.VideoFeed -> stringResource(R.string.preset_video_feed)
    PresetType.Reading -> stringResource(R.string.preset_reading)
    PresetType.Webtoon -> stringResource(R.string.preset_webtoon)
    PresetType.HorizontalFeed -> stringResource(R.string.preset_horizontal_feed)
    PresetType.Custom -> stringResource(R.string.preset_custom)
}
