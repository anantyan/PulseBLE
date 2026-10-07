package com.anantyan.pulseble.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = TextPrimary,
    primaryContainer = ElectricBlueDark,
    onPrimaryContainer = TextPrimary,
    secondary = RadarEmerald,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceElevated,
    onSecondaryContainer = RadarEmeraldLight,
    tertiary = NeonCyan,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricBlue,
    onPrimary = TextPrimary,
    secondary = RadarEmerald,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun PulseBLETheme(
    darkTheme: Boolean = true, // Default to sleek tactical dark mode
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
