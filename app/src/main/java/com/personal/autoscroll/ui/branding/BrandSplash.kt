package com.personal.autoscroll.ui.branding

import android.view.Window
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.personal.autoscroll.R
import kotlinx.coroutines.delay

/** Figma 3:66. Only shown on a fresh activity launch, never when returning from permissions. */
@OptIn(ExperimentalTextApi::class)
@Composable
fun BrandLaunch(window: Window, content: @Composable () -> Unit) {
    var showing by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(650L)
        showing = false
    }
    if (!showing) { content(); return }
    val view = LocalView.current
    SideEffect {
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
    }
    BoxWithConstraints(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xFFF7FBFF), Color(0xFFD6EDFF))))) {
        // The 360×800 reference places the icon at 75×245 with size 210.
        val iconSize = minOf(maxWidth * (210f / 360f), maxHeight * (210f / 800f))
        val iconTop = maxHeight * (245f / 800f)
        Image(
            painter = painterResource(R.drawable.brand_splash_icon), contentDescription = null,
            modifier = Modifier.offset(x = (maxWidth - iconSize) / 2, y = iconTop).size(iconSize),
        )
        Text(
            text = stringResource(R.string.app_name), color = Color(0xFF0A2661),
            fontSize = 28.sp, fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily(Font(R.font.inter, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600)))),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().offset(y = maxHeight * (486f / 800f)),
        )
    }
}
