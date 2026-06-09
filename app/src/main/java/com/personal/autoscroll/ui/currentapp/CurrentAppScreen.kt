package com.personal.autoscroll.ui.currentapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personal.autoscroll.R
import com.personal.autoscroll.domain.model.AppProfile
import com.personal.autoscroll.domain.model.IntentDirection
import com.personal.autoscroll.domain.model.ScrollMode
import com.personal.autoscroll.ui.foundation.LocalizedFormatters

@Composable
fun CurrentAppScreen(
    viewModel: CurrentAppViewModel,
    onShowOverlay: () -> Unit,
    onHideOverlay: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.nav_current_app),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            PermissionCard(
                title = stringResource(R.string.action_accessibility),
                value = if (state.permissionState.accessibilityConnected) {
                    stringResource(R.string.value_connected)
                } else {
                    stringResource(R.string.value_required)
                },
                isReady = state.permissionState.accessibilityConnected,
                onClick = viewModel::openAccessibilitySettings,
                modifier = Modifier.weight(1f),
            )
            PermissionCard(
                title = stringResource(R.string.label_overlay_permission),
                value = if (state.permissionState.overlayPermissionGranted) {
                    stringResource(R.string.value_granted)
                } else {
                    stringResource(R.string.value_required)
                },
                isReady = state.permissionState.overlayPermissionGranted,
                onClick = viewModel::openOverlaySettings,
                modifier = Modifier.weight(1f),
            )
        }

        ProfileCard(
            profile = state.profile,
            foregroundPackage = state.foregroundPackage,
            foregroundAppName = state.foregroundAppName,
        )

        QuickActionsCard(
            onTest = viewModel::testGesture,
            onSave = viewModel::saveProfile,
            onShowOverlay = onShowOverlay,
            onHideOverlay = onHideOverlay,
        )

        state.message?.let {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = it.arg?.let { arg -> stringResource(it.resId, arg) } ?: stringResource(it.resId),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                )
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun PermissionCard(
    title: String,
    value: String,
    isReady: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val readyColor = if (isReady) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
    val containerColor = readyColor.copy(alpha = if (isReady) 0.13f else 0.10f)

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(1.dp, readyColor.copy(alpha = 0.22f)),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(readyColor),
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun ProfileCard(
    profile: AppProfile?,
    foregroundPackage: String?,
    foregroundAppName: String?,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = foregroundAppName ?: profile?.appName ?: stringResource(R.string.value_unknown),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    PackagePill(foregroundPackage ?: profile?.packageName ?: stringResource(R.string.value_unknown))
                }
                StatusPill(
                    text = if (profile != null) {
                        stringResource(R.string.value_active_profile)
                    } else {
                        stringResource(R.string.value_no_profile)
                    },
                    active = profile != null,
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatItem(
                    label = stringResource(R.string.label_gesture),
                    value = profile?.gestureConfig?.intentDirection?.label()
                        ?: stringResource(R.string.value_unknown),
                    modifier = Modifier.weight(1f),
                )
                StatItem(
                    label = stringResource(R.string.label_timing),
                    value = profile?.timingConfig?.mode?.label()
                        ?: stringResource(R.string.value_unknown),
                    modifier = Modifier.weight(1f),
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatItem(
                    label = stringResource(R.string.label_speed),
                    value = profile?.timingConfig?.delayMillis?.let(LocalizedFormatters::seconds)
                        ?: stringResource(R.string.value_unknown),
                    modifier = Modifier.weight(1f),
                )
                StatItem(
                    label = stringResource(R.string.label_distance),
                    value = profile?.gestureConfig?.distancePercent?.let { "$it%" }
                        ?: stringResource(R.string.value_unknown),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun QuickActionsCard(
    onTest: () -> Unit,
    onSave: () -> Unit,
    onShowOverlay: () -> Unit,
    onHideOverlay: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.label_quick_actions).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onShowOverlay,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(13.dp),
                ) {
                    Text(stringResource(R.string.action_show_overlay), maxLines = 1)
                }
                OutlinedButton(
                    onClick = onHideOverlay,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(13.dp),
                ) {
                    Text(stringResource(R.string.action_hide_overlay), maxLines = 1)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onTest,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                ) {
                    Text(stringResource(R.string.action_test), maxLines = 1)
                }
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(13.dp),
                ) {
                    Text(stringResource(R.string.action_save), maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun PackagePill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 7.dp, vertical = 3.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

@Composable
private fun StatusPill(text: String, active: Boolean) {
    val color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .border(1.dp, color.copy(alpha = 0.25f), CircleShape)
            .background(color.copy(alpha = 0.10f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color,
            maxLines = 1,
        )
    }
}

@Composable
private fun StatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
    }
}

@Composable
private fun IntentDirection.label(): String = when (this) {
    IntentDirection.NextItem -> stringResource(R.string.direction_next_item)
    IntentDirection.PreviousItem -> stringResource(R.string.direction_previous_item)
}

@Composable
private fun ScrollMode.label(): String = when (this) {
    ScrollMode.Once -> stringResource(R.string.mode_once)
    ScrollMode.Repeat -> stringResource(R.string.mode_repeat)
    ScrollMode.UntilStop -> stringResource(R.string.mode_until_stop)
    ScrollMode.Timer -> stringResource(R.string.mode_timer)
}
