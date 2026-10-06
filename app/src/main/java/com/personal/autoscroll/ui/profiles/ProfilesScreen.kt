package com.personal.autoscroll.ui.profiles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personal.autoscroll.ui.foundation.*
import com.personal.autoscroll.R
import com.personal.autoscroll.domain.model.AppProfile
import com.personal.autoscroll.ui.automation.ProfileEditorContent
import com.personal.autoscroll.ui.foundation.LocalizedFormatters

@Composable
fun ProfilesScreen(viewModel: ProfilesViewModel, onConfigureApp: () -> Unit) {
    val profiles by viewModel.profiles.collectAsStateWithLifecycle()
    val editState by viewModel.editState.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<AppProfile?>(null) }
    var pendingReset by remember { mutableStateOf<AppProfile?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(R.string.nav_profiles),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(stringResource(R.string.profiles_description), style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(R.string.profile_usage_help), style = MaterialTheme.typography.bodySmall)
        OutlinedButton(onClick = onConfigureApp) { Text(stringResource(R.string.choose_app)) }
        if (profiles.isEmpty()) {
            Text(
                text = stringResource(R.string.profiles_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            profiles.forEach { profile ->
                ProfileRow(
                    profile = profile,
                    onEdit = { viewModel.beginEdit(profile) },
                    onDelete = { pendingDelete = profile },
                    onReset = { pendingReset = profile },
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }

    if (editState.draft != null) ProfileEditorDialog(viewModel, editState)

    pendingDelete?.let { profile ->
        ConfirmProfileActionDialog(
            title = stringResource(R.string.profile_delete_title),
            message = stringResource(R.string.profile_delete_message, appDisplayName(profile.appName, profile.packageName)),
            confirmLabel = stringResource(R.string.action_delete),
            onDismiss = { pendingDelete = null },
            onConfirm = {
                viewModel.deleteProfile(profile)
                pendingDelete = null
            },
        )
    }

    pendingReset?.let { profile ->
        ConfirmProfileActionDialog(
            title = stringResource(R.string.profile_reset_title),
            message = stringResource(R.string.profile_reset_message, appDisplayName(profile.appName, profile.packageName)),
            confirmLabel = stringResource(R.string.action_reset),
            onDismiss = { pendingReset = null },
            onConfirm = {
                viewModel.resetProfile(profile)
                pendingReset = null
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileRow(
    profile: AppProfile,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onReset: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.small,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AppIdentity(profile)
            Text(scrollSummary(profile), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(profile.profileStatus.labelRes()), style = MaterialTheme.typography.bodySmall)
            AppPackageDetails(profile.packageName)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(onClick = onEdit) {
                    Text(stringResource(R.string.action_edit))
                }
                TextButton(onClick = onReset) {
                    Text(stringResource(R.string.action_reset))
                }
                TextButton(onClick = onDelete) {
                    Text(stringResource(R.string.action_delete))
                }
            }
        }
    }
}

@Composable
private fun ConfirmProfileActionDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}
