package com.kakdela.p2p.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ---------------- Neon Palette ----------------

val NeonPink = Color(0xFFFF00FF)
val NeonCyan = Color(0xFF00FFFF)
val NeonBlue = Color(0xFF00D4FF)
val NeonPurple = Color(0xFF8A2BE2)
val NeonGreen = Color(0xFF39FF14)

val BackgroundDark = Color(0xFF0A0A0A)
val SurfaceDark = Color(0xFF121212)
val OnBackground = Color(0xFFE0E0E0)
val OnSurface = Color(0xFFFFFFFF)

// ---------------- Color Scheme ----------------

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    secondary = NeonPink,
    tertiary = NeonBlue,

    background = BackgroundDark,
    surface = SurfaceDark,
    onBackground = OnBackground,
    onSurface = OnSurface,
)

@Composable
fun KakdelaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}
