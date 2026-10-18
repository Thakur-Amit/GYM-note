package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val GymDarkColorScheme = darkColorScheme(
    primary = PrimaryPurple,
    onPrimary = PrimaryText,
    primaryContainer = SecondaryIndigo,
    onPrimaryContainer = PrimaryText,
    secondary = SecondaryIndigo,
    onSecondary = PrimaryText,
    secondaryContainer = ElevatedSurface,
    onSecondaryContainer = PrimaryText,
    background = BackgroundDark,
    onBackground = PrimaryText,
    surface = PrimaryCard,
    onSurface = PrimaryText,
    surfaceVariant = SecondaryCard,
    onSurfaceVariant = SecondaryText,
    outline = DividerDark,
    outlineVariant = ElevatedSurface,
    error = AccentError,
    onError = PrimaryText
)

@Composable
fun GymTrackerTheme(
    content: @Composable () -> Unit
) {
    // Gym Tracker enforces the established premium dark aesthetic
    MaterialTheme(
        colorScheme = GymDarkColorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias for template
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    GymTrackerTheme(content = content)
}
