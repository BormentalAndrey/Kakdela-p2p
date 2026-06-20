package com.vasilisinaazbuka.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Тема приложения «Василисина азбука»
 * Использует Material 3 с яркими сказочными цветами
 */

private val LightColorScheme = lightColorScheme(
    primary = FairyBlue,
    onPrimary = FairyWhite,
    primaryContainer = FairyLightBlue,
    onPrimaryContainer = FairyBlue,

    secondary = FairyPurple,
    onSecondary = FairyWhite,
    secondaryContainer = FairyLightPurple,
    onSecondaryContainer = FairyPurple,

    tertiary = FairyGreen,
    onTertiary = FairyWhite,
    tertiaryContainer = FairyLightGreen,
    onTertiaryContainer = FairyGreen,

    error = FairyPink,
    onError = FairyWhite,
    errorContainer = FairyLightPink,
    onErrorContainer = FairyPink,

    background = FairyWhite,
    onBackground = FairyBlue,
    surface = FairyWhite,
    onSurface = FairyBlue,
    surfaceVariant = FairyLightGold,
    onSurfaceVariant = FairyGold,
    outline = FairyGray
)

@Composable
fun VasilisinaAzbukaTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = FairyBlue.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
