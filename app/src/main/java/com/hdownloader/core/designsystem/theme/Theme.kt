package com.hdownloader.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Indigo40,
    onPrimary = LightBackground,
    primaryContainer = Indigo30,
    onPrimaryContainer = Indigo80,
    secondary = Cyan80,
    onSecondary = DarkBackground,
    secondaryContainer = Cyan60,
    onSecondaryContainer = DarkBackground,
    tertiary = Cyan80,
    background = DarkBackground,
    onBackground = LightSurfaceVariant,
    surface = DarkSurface,
    onSurface = LightSurfaceVariant,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = LightOutline,
    surfaceContainer = DarkSurface,
    surfaceContainerLow = DarkBackground,
    surfaceContainerHigh = DarkSurfaceHigh,
    surfaceContainerHighest = DarkSurfaceHigh,
    outline = DarkOutline,
    error = ErrorRed,
    onError = LightBackground,
)

private val LightColorScheme = lightColorScheme(
    primary = Indigo30,
    onPrimary = LightBackground,
    primaryContainer = Indigo40,
    onPrimaryContainer = LightBackground,
    secondary = Cyan40,
    onSecondary = LightBackground,
    secondaryContainer = Cyan80,
    onSecondaryContainer = DarkBackground,
    background = LightBackground,
    onBackground = DarkSurface,
    surface = LightSurface,
    onSurface = DarkSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOutline,
    outline = LightOutline,
    error = ErrorRed,
)

/**
 * Applies the H Downloader theme.
 *
 * @param darkTheme Force a specific theme; when null it follows the system.
 * @param dynamicColor Enable Material You dynamic colors (Android 12+).
 */
@Composable
fun HDTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HDTypography,
        shapes = HDShapes,
        content = content,
    )
}
