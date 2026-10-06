package com.personal.autoscroll.ui.foundation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.personal.autoscroll.R
import com.personal.autoscroll.domain.model.*

fun ProfileStatus.labelRes(): Int = when (this) {
    ProfileStatus.Untested -> R.string.status_untested
    ProfileStatus.Tested -> R.string.status_tested
    ProfileStatus.NeedsReview -> R.string.status_needs_review
}
fun PresetType.labelRes(): Int = when (this) {
    PresetType.VideoFeed -> R.string.preset_video_feed
    PresetType.Reading -> R.string.preset_reading
    PresetType.Webtoon -> R.string.preset_webtoon
    PresetType.HorizontalFeed -> R.string.preset_horizontal_feed
    PresetType.Custom -> R.string.preset_custom
}
fun ScrollMode.labelRes(): Int = when (this) {
    ScrollMode.Once -> R.string.mode_once
    ScrollMode.Repeat -> R.string.mode_repeat
    ScrollMode.UntilStop -> R.string.mode_until_stop
    ScrollMode.Timer -> R.string.mode_timer
}
fun OverlaySize.labelRes(): Int = when (this) {
    OverlaySize.Small -> R.string.size_small
    OverlaySize.Medium -> R.string.size_medium
    OverlaySize.Large -> R.string.size_large
}
@Composable
fun appDisplayName(name: String?, packageName: String?): String {
    val context = androidx.compose.ui.platform.LocalContext.current
    val resolved = remember(name, packageName) {
        name?.takeIf { it.isNotBlank() && it != packageName } ?: packageName?.let {
            runCatching { context.packageManager.getApplicationLabel(context.packageManager.getApplicationInfo(it, 0)).toString() }.getOrNull()
        }
    }
    return resolved ?: stringResource(R.string.app_name_unavailable)
}

@Composable
fun AppPackageDetails(packageName: String?) {
    if (packageName == null) return
    var expanded by remember(packageName) { mutableStateOf(false) }
    Column {
        TextButton(onClick = { expanded = !expanded }) {
            Text(stringResource(if (expanded) R.string.action_hide_details else R.string.action_details))
        }
        if (expanded) {
            Text(stringResource(R.string.app_package_detail, packageName))
            Text(stringResource(R.string.gesture_test_explanation), style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
        }
    }
}


@Composable
fun AppIdentity(profile: AppProfile) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val icon = remember(profile.packageName) {
        runCatching {
            context.packageManager.getApplicationIcon(profile.packageName).toBitmap(96, 96)
        }.getOrNull()
    }
    androidx.compose.foundation.layout.Row(
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
    ) {
        if (icon != null) androidx.compose.foundation.Image(
            bitmap = icon.asImageBitmap(), contentDescription = null,
            modifier = androidx.compose.ui.Modifier.size(40.dp),
        )
        Text(appDisplayName(profile.appName, profile.packageName), style = androidx.compose.material3.MaterialTheme.typography.titleMedium, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
    }
}

@Composable
fun UnsavedChangesDialog(onKeepEditing: () -> Unit, onDiscard: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onKeepEditing,
        title = { Text(stringResource(R.string.unsaved_title)) },
        text = { Text(stringResource(R.string.unsaved_message)) },
        confirmButton = { TextButton(onClick = onDiscard) { Text(stringResource(R.string.action_discard)) } },
        dismissButton = { TextButton(onClick = onKeepEditing) { Text(stringResource(R.string.keep_editing)) } },
    )
}

@Composable
fun scrollSummary(profile: AppProfile): String {
    val gesture = profile.gestureConfig
    val next = gesture.intentDirection == IntentDirection.NextItem
    val forward = next != gesture.invertPhysicalDirection
    val direction = if (gesture.axis == GestureAxis.Vertical) {
        if (forward) R.string.swipe_up else R.string.swipe_down
    } else {
        if (forward) R.string.swipe_left else R.string.swipe_right
    }
    val mode = when (profile.timingConfig.mode) {
        ScrollMode.Repeat -> stringResource(R.string.summary_repeat, profile.timingConfig.repeatCount ?: 1)
        ScrollMode.Timer -> stringResource(R.string.summary_timer, LocalizedFormatters.seconds(profile.timingConfig.durationMillis ?: 0))
        else -> stringResource(profile.timingConfig.mode.labelRes())
    }
    return if (profile.timingConfig.mode == ScrollMode.Once) {
        stringResource(R.string.summary_once, stringResource(direction), mode)
    } else stringResource(R.string.scroll_summary, stringResource(direction), LocalizedFormatters.seconds(profile.timingConfig.delayMillis), mode)
}
