package com.personal.autoscroll.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.personal.autoscroll.R
import com.personal.autoscroll.ui.advanced.AdvancedScreen
import com.personal.autoscroll.ui.advanced.AdvancedViewModel
import com.personal.autoscroll.ui.automation.AutomationScreen
import com.personal.autoscroll.ui.automation.AutomationViewModel
import com.personal.autoscroll.ui.currentapp.CurrentAppScreen
import com.personal.autoscroll.ui.currentapp.CurrentAppViewModel
import com.personal.autoscroll.ui.overlaysettings.OverlaySettingsScreen
import com.personal.autoscroll.ui.overlaysettings.OverlaySettingsViewModel
import com.personal.autoscroll.ui.profiles.ProfilesScreen
import com.personal.autoscroll.ui.profiles.ProfilesViewModel

enum class AppDestination(
    @param:StringRes val labelRes: Int,
) {
    CurrentApp(R.string.nav_current),
    Profiles(R.string.nav_profiles),
    Automation(R.string.nav_automation_tab),
    Overlay(R.string.nav_overlay),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    profilesViewModel: ProfilesViewModel,
    currentAppViewModel: CurrentAppViewModel,
    automationViewModel: AutomationViewModel,
    advancedViewModel: AdvancedViewModel,
    overlaySettingsViewModel: OverlaySettingsViewModel,
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
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = stringResource(R.string.app_name),
                                    style = MaterialTheme.typography.titleLarge,
                                )
                                Text(
                                    text = stringResource(R.string.app_tagline),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.onBackground,
                        ),
                    )
                    PrimaryTabRow(
                        selectedTabIndex = destination.ordinal,
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.primary,
                    ) {
                        AppDestination.entries.forEach { item ->
                            Tab(
                                selected = item == destination,
                                onClick = { destination = item },
                                text = { Text(stringResource(item.labelRes), maxLines = 1) },
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    when (destination) {
                        AppDestination.CurrentApp -> CurrentAppScreen(
                            viewModel = currentAppViewModel,
                            onShowOverlay = onShowOverlay,
                            onHideOverlay = onHideOverlay,
                        )
                        AppDestination.Profiles -> ProfilesScreen(profilesViewModel)
                        AppDestination.Automation -> AutomationScreen(automationViewModel)
                        AppDestination.Overlay -> OverlayScreen(
                            overlaySettingsViewModel = overlaySettingsViewModel,
                            advancedViewModel = advancedViewModel,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OverlayScreen(
    overlaySettingsViewModel: OverlaySettingsViewModel,
    advancedViewModel: AdvancedViewModel,
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        OverlaySettingsScreen(viewModel = overlaySettingsViewModel, showTitle = true)
        AdvancedScreen(viewModel = advancedViewModel, showTitle = true)
    }
}
