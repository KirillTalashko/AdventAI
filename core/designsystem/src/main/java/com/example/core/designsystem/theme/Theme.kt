package com.example.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = AdventPrimary,
    onPrimary = AdventOnPrimary,
    secondary = AdventSecondary,
    tertiary = AdventTertiary
)

private val DarkColorScheme = darkColorScheme(
    primary = AdventPrimary,
    onPrimary = AdventOnPrimary,
    secondary = AdventSecondary,
    tertiary = AdventTertiary
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
