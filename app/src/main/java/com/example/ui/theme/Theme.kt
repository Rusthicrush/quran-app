package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = DeepMidnight,
    secondary = GoldAccent,
    onSecondary = DeepMidnight,
    tertiary = TamilColor,
    onTertiary = DeepMidnight,
    background = DeepMidnight,
    surface = CardBackground,
    onBackground = LightText,
    onSurface = LightText,
    outline = BorderColor,
    surfaceVariant = CardBackground,
    onSurfaceVariant = LightText
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme for cohesive Islamic look
    dynamicColor: Boolean = false, // Disable dynamic colors to keep golden aesthetic
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

