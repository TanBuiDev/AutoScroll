package com.personal.autoscroll.ui.currentapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personal.autoscroll.R
import com.personal.autoscroll.ui.foundation.LocalizedFormatters

@Composable
fun CurrentAppScreen(
    viewModel: CurrentAppViewModel,
    onShowOverlay: () -> Unit,
    onHideOverlay: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.nav_current_app),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        StatusLine(
            stringResource(R.string.label_accessibility_enabled),
            if (state.permissionState.accessibilityEnabled) stringResource(R.string.value_yes) else stringResource(R.string.value_no),
        )
        StatusLine(
            stringResource(R.string.label_accessibility_service),
            if (state.permissionState.accessibilityConnected) stringResource(R.string.value_connected) else stringResource(R.string.value_not_connected),
        )
        StatusLine(
            stringResource(R.string.label_overlay_permission),
            if (state.permissionState.overlayPermissionGranted) stringResource(R.string.value_granted) else stringResource(R.string.value_not_granted),
        )
        StatusLine(stringResource(R.string.label_foreground_package), state.foregroundPackage ?: stringResource(R.string.value_unknown))
        StatusLine(stringResource(R.string.label_foreground_app), state.foregroundAppName ?: stringResource(R.string.value_unknown))

        state.profile?.let { profile ->
            StatusLine(stringResource(R.string.label_mode), profile.timingConfig.mode.toString())
            StatusLine(stringResource(R.string.label_direction), profile.gestureConfig.intentDirection.toString())
            StatusLine(stringResource(R.string.label_delay), LocalizedFormatters.seconds(profile.timingConfig.delayMillis))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = viewModel::openAccessibilitySettings) {
                Text(stringResource(R.string.action_accessibility), maxLines = 2)
            }
            OutlinedButton(onClick = viewModel::openOverlaySettings) {
                Text(stringResource(R.string.action_overlay_permission), maxLines = 2)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = viewModel::testGesture) {
                Text(stringResource(R.string.action_test), maxLines = 2)
            }
            Button(onClick = viewModel::saveProfile) {
                Text(stringResource(R.string.action_save), maxLines = 2)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onShowOverlay) {
                Text(stringResource(R.string.action_show_overlay), maxLines = 2)
            }
            OutlinedButton(onClick = onHideOverlay) {
                Text(stringResource(R.string.action_hide_overlay), maxLines = 2)
            }
        }

        state.message?.let {
            Text(
                text = it.arg?.let { arg -> stringResource(it.resId, arg) } ?: stringResource(it.resId),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun StatusLine(label: String, value: String) {
    Text(
        text = "$label: $value",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
