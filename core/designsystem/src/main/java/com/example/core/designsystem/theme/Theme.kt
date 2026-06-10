package com.example.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = AdventPrimary,
    onPrimary = AdventOnPrimary,
    primaryContainer = AdventPrimaryContainer,
    onPrimaryContainer = AdventOnPrimaryContainer,
    secondary = AdventSecondary,
    secondaryContainer = AdventSecondaryContainer,
    onSecondaryContainer = AdventOnSecondaryContainer,
    tertiary = AdventTertiary,
    tertiaryContainer = AdventTertiaryContainer,
    onTertiaryContainer = AdventOnTertiaryContainer,
    background = AdventBackground,
    surface = AdventSurface,
    surfaceContainer = AdventSurfaceContainer,
    surfaceContainerLow = AdventSurface,
    surfaceVariant = AdventSurfaceVariant,
    outlineVariant = AdventOutlineVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = AdventPrimary,
    onPrimary = AdventOnPrimary,
    primaryContainer = AdventOnPrimaryContainer,
    onPrimaryContainer = AdventPrimaryContainer,
    secondary = AdventSecondary,
    secondaryContainer = AdventOnSecondaryContainer,
    onSecondaryContainer = AdventSecondaryContainer,
    tertiary = AdventTertiary,
    tertiaryContainer = AdventOnTertiaryContainer,
    onTertiaryContainer = AdventTertiaryContainer
)

@Composable
fun AdventAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content
    )
}
