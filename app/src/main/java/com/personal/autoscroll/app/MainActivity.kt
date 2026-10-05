package com.personal.autoscroll.app

import android.os.Bundle
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import android.view.Window
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.autoscroll.core.localization.AppLocale
import com.personal.autoscroll.core.overlay.OverlayAutomationCoordinator
import com.personal.autoscroll.data.datastore.SettingsDataStore
import com.personal.autoscroll.domain.model.GlobalSettings
import com.personal.autoscroll.domain.model.GestureAxis
import com.personal.autoscroll.domain.model.IntentDirection
import com.personal.autoscroll.domain.model.ScrollMode
import com.personal.autoscroll.domain.model.ThemeMode
import com.personal.autoscroll.ui.AppNavigation
import com.personal.autoscroll.ui.advanced.AdvancedViewModel
import com.personal.autoscroll.ui.currentapp.CurrentAppViewModel
import com.personal.autoscroll.ui.overlaysettings.OverlaySettingsViewModel
import com.personal.autoscroll.ui.profiles.ProfilesViewModel
import com.personal.autoscroll.ui.theme.AutoScrollTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private val profilesViewModel: ProfilesViewModel by viewModels()
    private val currentAppViewModel: CurrentAppViewModel by viewModels()
    private val advancedViewModel: AdvancedViewModel by viewModels()
    private val overlaySettingsViewModel: OverlaySettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        setContent {
            AutoScrollApp(
                window = window,
                viewModel = viewModel,
                profilesViewModel = profilesViewModel,
                currentAppViewModel = currentAppViewModel,
                advancedViewModel = advancedViewModel,
                overlaySettingsViewModel = overlaySettingsViewModel,
                onGestureAxisChanged = viewModel::updateGestureAxis,
                onIntentDirectionChanged = viewModel::updateIntentDirection,
                onGestureDistanceChanged = viewModel::updateGestureDistance,
                onSwipeDurationChanged = viewModel::updateSwipeDuration,
                onShowOverlay = { viewModel.showOverlay() },
                onHideOverlay = { viewModel.hideOverlay() },
            )
        }
    }

    override fun onResume() {
        super.onResume()
        currentAppViewModel.refreshPermissions()
    }

}

@Composable
private fun AutoScrollApp(
    window: Window,
    viewModel: MainViewModel,
    profilesViewModel: ProfilesViewModel,
    currentAppViewModel: CurrentAppViewModel,
    advancedViewModel: AdvancedViewModel,
    overlaySettingsViewModel: OverlaySettingsViewModel,
    onGestureAxisChanged: (GestureAxis) -> Unit,
    onIntentDirectionChanged: (IntentDirection) -> Unit,
    onGestureDistanceChanged: (Int) -> Unit,
    onSwipeDurationChanged: (Long) -> Unit,
    onShowOverlay: () -> Unit,
    onHideOverlay: () -> Unit,
) {
    val settings = viewModel.settings.collectAsStateWithLifecycle()
    val baseConfiguration = LocalConfiguration.current
    val baseContext = LocalContext.current
    val systemDarkTheme = isSystemInDarkTheme()
    val useDarkTheme = when (settings.value.themeMode) {
        ThemeMode.System -> systemDarkTheme
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    val view = LocalView.current
    val localizedContext = remember(baseContext, settings.value.languageMode) {
        AppLocale.localizedContext(baseContext, settings.value.languageMode)
    }
    val localizedConfiguration = remember(baseConfiguration, settings.value.languageMode) {
        AppLocale.localizedConfiguration(baseConfiguration, settings.value.languageMode)
    }

    SideEffect {
        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.isAppearanceLightStatusBars = !useDarkTheme
        insetsController.isAppearanceLightNavigationBars = !useDarkTheme
    }

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedConfiguration,
    ) {
        AutoScrollTheme(themeMode = settings.value.themeMode) {
            AppNavigation(
                profilesViewModel = profilesViewModel,
                currentAppViewModel = currentAppViewModel,
                advancedViewModel = advancedViewModel,
                overlaySettingsViewModel = overlaySettingsViewModel,
                onGestureAxisChanged = onGestureAxisChanged,
                onIntentDirectionChanged = onIntentDirectionChanged,
                onGestureDistanceChanged = onGestureDistanceChanged,
                onSwipeDurationChanged = onSwipeDurationChanged,
                onShowOverlay = onShowOverlay,
                onHideOverlay = onHideOverlay,
                onScrollModeChanged = viewModel::updateScrollMode,
                onDelayChanged = viewModel::updateDelayMillis,
                onRepeatCountChanged = viewModel::updateRepeatCount,
                onDurationChanged = viewModel::updateDurationMillis,
            )
        }
    }
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val overlayAutomationCoordinator: OverlayAutomationCoordinator,
    settingsDataStore: SettingsDataStore,
) : ViewModel() {
    val settings = settingsDataStore.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GlobalSettings.Default,
    )

    fun showOverlay() {
        overlayAutomationCoordinator.showOverlay(
            languageMode = settings.value.languageMode,
        )
    }

    fun hideOverlay() {
        overlayAutomationCoordinator.hideOverlay()
    }

    fun updateGestureAxis(axis: GestureAxis) {
        overlayAutomationCoordinator.updateGestureAxis(axis)
    }

    fun updateIntentDirection(direction: IntentDirection) {
        overlayAutomationCoordinator.updateIntentDirection(direction)
    }

    fun updateGestureDistance(distancePercent: Int) {
        overlayAutomationCoordinator.updateGestureDistance(distancePercent)
    }

    fun updateSwipeDuration(durationMillis: Long) {
        overlayAutomationCoordinator.updateSwipeDuration(durationMillis)
    }

    fun updateScrollMode(mode: ScrollMode) {
        overlayAutomationCoordinator.updateScrollMode(mode)
    }

    fun updateDelayMillis(delayMillis: Long) {
        overlayAutomationCoordinator.updateDelayMillis(delayMillis)
    }

    fun updateRepeatCount(repeatCount: Int) {
        overlayAutomationCoordinator.updateRepeatCount(repeatCount)
    }

    fun updateDurationMillis(durationMillis: Long) {
        overlayAutomationCoordinator.updateDurationMillis(durationMillis)
    }
}
