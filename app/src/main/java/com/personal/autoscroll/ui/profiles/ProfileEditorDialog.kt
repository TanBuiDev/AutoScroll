package com.personal.autoscroll.ui.profiles

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.personal.autoscroll.R
import com.personal.autoscroll.ui.automation.ProfileEditorContent
import com.personal.autoscroll.ui.foundation.*

@Composable
fun ProfileEditorDialog(viewModel: ProfilesViewModel, editState: ProfileEditState) {
    val draft = editState.draft ?: return
    var confirmDiscard by remember { mutableStateOf(false) }
    val requestClose = { if (!editState.isSaving) { if (editState.isDirty) confirmDiscard = true else viewModel.cancelEdit() } }
    Dialog(onDismissRequest = requestClose, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        val dialogView = androidx.compose.ui.platform.LocalView.current
        val surfaceColor = MaterialTheme.colorScheme.surface
        SideEffect {
            val window = (dialogView.parent as? androidx.compose.ui.window.DialogWindowProvider)?.window
            if (window != null) {
                val controller = androidx.core.view.WindowCompat.getInsetsController(window, dialogView)
                controller.isAppearanceLightStatusBars = surfaceColor.luminance() > 0.5f
                controller.isAppearanceLightNavigationBars = surfaceColor.luminance() > 0.5f
            }
        }
        BackHandler(onBack = requestClose)
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = requestClose, enabled = !editState.isSaving) { Text(stringResource(R.string.action_back)) }
                    Text(stringResource(R.string.action_edit), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp))
                }
                Column(Modifier.padding(horizontal = 16.dp)) {
                    AppIdentity(draft)
                    Text(stringResource(R.string.profile_name_explanation), style = MaterialTheme.typography.bodySmall)
                }
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AppPackageDetails(draft.packageName)
                    Text(stringResource(R.string.profile_usage_help), style = MaterialTheme.typography.bodySmall)
                    ProfileEditorContent(profile = draft, onProfileChange = viewModel::updateEditingProfile)
                }
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    editState.error?.let { Text(stringResource(R.string.save_error), color = MaterialTheme.colorScheme.error) }
                    if (editState.isDirty) Text(stringResource(R.string.profile_unsaved_changes), style = MaterialTheme.typography.bodySmall)
                    Button(onClick = viewModel::saveEditingProfile, enabled = editState.isDirty && !editState.isSaving, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(if (editState.isSaving) R.string.action_saving else R.string.action_save_changes))
                    }
                }
            }
        }
        if (confirmDiscard) UnsavedChangesDialog(onKeepEditing = { confirmDiscard = false }, onDiscard = { confirmDiscard = false; viewModel.cancelEdit() })
    }
}
