package com.personal.autoscroll.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.annotation.StringRes
import com.personal.autoscroll.R
import com.personal.autoscroll.ui.advanced.AdvancedScreen
import com.personal.autoscroll.ui.advanced.AdvancedViewModel
import com.personal.autoscroll.ui.currentapp.CurrentAppScreen
import com.personal.autoscroll.ui.currentapp.CurrentAppViewModel
import com.personal.autoscroll.ui.foundation.AppScreen
import com.personal.autoscroll.ui.gesture.GestureScreen
import com.personal.autoscroll.ui.overlaysettings.OverlaySettingsScreen
import com.personal.autoscroll.ui.profiles.ProfilesScreen
import com.personal.autoscroll.ui.profiles.ProfilesViewModel
import com.personal.autoscroll.ui.timing.TimingScreen

enum class AppDestination(
    @param:StringRes val labelRes: Int,
) {
    Profiles(R.string.nav_profiles),
    CurrentApp(R.string.nav_current_app),
    Gesture(R.string.nav_gesture),
    Timing(R.string.nav_timing),
    Overlay(R.string.nav_overlay),
    Advanced(R.string.nav_advanced),
}

@Composable
fun AppNavigation(
    profilesViewModel: ProfilesViewModel,
    currentAppViewModel: CurrentAppViewModel,
    advancedViewModel: AdvancedViewModel,
    onShowOverlay: () -> Unit,
    onHideOverlay: () -> Unit,
) {
    var destination by remember { mutableStateOf(AppDestination.CurrentApp) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        color = MaterialTheme.colorScheme.background,
    ) {
        AppScreen {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AppDestination.entries.forEach { item ->
                    DestinationButton(
                        label = stringResource(item.labelRes),
                        selected = item == destination,
                        onClick = { destination = item },
                    )
                }
            }

            when (destination) {
                AppDestination.Profiles -> ProfilesScreen(profilesViewModel)
                AppDestination.CurrentApp -> CurrentAppScreen(
                    viewModel = currentAppViewModel,
                    onShowOverlay = onShowOverlay,
                    onHideOverlay = onHideOverlay,
                )
                AppDestination.Gesture -> GestureScreen()
                AppDestination.Timing -> TimingScreen()
                AppDestination.Overlay -> OverlaySettingsScreen()
                AppDestination.Advanced -> AdvancedScreen(advancedViewModel)
            }
        }
    }
}

@Composable
private fun DestinationButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            },
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Text(label)
    }
}
