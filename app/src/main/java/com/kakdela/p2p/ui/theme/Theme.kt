package com.kakdela.p2p.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Неоновые цвета — без дубликатов!
private val NeonCyan = Color(0xFF00FFF0)
private val NeonPink = Color(0xFFFF00C8)
private val NeonPurple = Color(0xFFD700FF)
private val NeonBlue = Color(0xFF0088FF)

private val BackgroundDark = Color(0xFF0A0A0A)
private val SurfaceDark = Color(0xFF121212)
private val OnBackground = Color(0xFFE0E0E0)
private val OnSurface = Color(0xFFFFFFFF)

// Неоновая схема (тёмная)
private val KakdelaColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color.Black,
    primaryContainer = NeonCyan.copy(alpha = 0.2f),
    onPrimaryContainer = NeonCyan,

    secondary = NeonPink,
    onSecondary = Color.Black,
    secondaryContainer = NeonPink.copy(alpha = 0.15f),

    tertiary = NeonPurple,
    onTertiary = Color.Black,

    background = BackgroundDark,
    onBackground = OnBackground,

    surface = SurfaceDark,
    onSurface = OnSurface,
    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = Color(0xFFAAAAAA),

    outline = NeonBlue,
    outlineVariant = NeonBlue.copy(alpha = 0.5f),

    error = Color(0xFFFF5555),
    onError = Color.Black
)

private val KakdelaTypography = Typography()

@Composable
fun KakdelaTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as Activity).window.apply {
                statusBarColor = BackgroundDark.toArgb()
                navigationBarColor = BackgroundDark.toArgb()
                WindowCompat.getInsetsController(this, view).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = KakdelaColorScheme,
        typography = KakdelaTypography,
        content = content
    )
}
