package com.babytracker.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = CardWhite,
    secondary = AccentGreen,
    background = SoftBackground,
    surface = CardWhite,
    onSurface = TextMain
)

private val DarkColors = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = CardWhite,
    secondary = AccentGreen
)

@Composable
fun BabyTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
