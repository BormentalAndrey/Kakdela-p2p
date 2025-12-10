package com.kakdela.p2p.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Неоновые фирменные цвета
private val NeonCyan = Color(0xFF00FFF0)
private val NeonPink = Color(0xFFFF00C8)
private val NeonPurple = Color(0xFFD700FF)
private val NeonBlue = Color(0xFF0088FF)

// Базовые фоновые цвета
private val BackgroundDark = Color(0xFF0A0A0A)
private val SurfaceDark = Color(0xFF121212)

// Цветовая схема приложения
private val KakdelaColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color.Black,
    primaryContainer = NeonCyan.copy(alpha = 0.22f),
    onPrimaryContainer = NeonCyan,

    secondary = NeonPink,
    onSecondary = Color.Black,
    secondaryContainer = NeonPink.copy(alpha = 0.18f),
    onSecondaryContainer = NeonPink,

    tertiary = NeonPurple,
    onTertiary = Color.Black,
    tertiaryContainer = NeonPurple.copy(alpha = 0.18f),
    onTertiaryContainer = NeonPurple,

    background = BackgroundDark,
    onBackground = Color(0xFFE0E0E0),

    surface = SurfaceDark,
    onSurface = Color.White,

    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = Color(0xFFAAAAAA),

    outline = NeonBlue,
    outlineVariant = NeonBlue.copy(alpha = 0.5f),

    error = Color(0xFFFF5555),
    onError = Color.Black
)

// Типографика
private val KakdelaTypography = Typography(
    bodyLarge = Typography().bodyLarge.copy(color = Color.White),
    titleLarge = Typography().titleLarge.copy(color = NeonCyan),
    labelLarge = Typography().labelLarge.copy(color = NeonPink)
)

// Функция получения Activity
private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

// Основная тема приложения
@Composable
fun KakdelaTheme(content: @Composable () -> Unit) {
    val view = LocalView.current

    LaunchedEffect(Unit) {
        view.context.findActivity()?.window?.let { window ->
            window.statusBarColor = BackgroundDark.toArgb()
            window.navigationBarColor = BackgroundDark.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = KakdelaColorScheme,
        typography = KakdelaTypography,
        content = content
    )
}
