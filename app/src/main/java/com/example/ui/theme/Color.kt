package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Established Gym Tracker Design Tokens
val BackgroundDark = Color(0xFF09090B)
val PrimaryCard = Color(0xFF141417)
val SecondaryCard = Color(0xFF1C1C21)
val ElevatedSurface = Color(0xFF24242A)

val PrimaryText = Color(0xFFFAFAFA)
val SecondaryText = Color(0xFFA1A1AA)
val MutedText = Color(0xFF71717A)

val PrimaryPurple = Color(0xFF8B5CF6)
val SecondaryIndigo = Color(0xFF4F46E5)
val PurpleGradientStart = Color(0xFFA855F7)
val PurpleGradientEnd = Color(0xFF4338CA)

// Neon Accents for Bento and Multi-Screen UI
val NeonCyan = Color(0xFF06B6D4)
val NeonEmerald = Color(0xFF10B981)
val NeonAmber = Color(0xFFF59E0B)
val NeonRose = Color(0xFFF43F5E)
val NeonViolet = Color(0xFFA855F7)
val NeonBlue = Color(0xFF3B82F6)

val CardBentoDark = Color(0xFF161619)
val CardBentoBorder = Color(0xFF23232A)

val AccentSuccess = Color(0xFF22C55E)
val AccentWarning = Color(0xFFF59E0B)
val AccentError = Color(0xFFF87171)
val DividerDark = Color(0xFF2A2A30)

// Gradients
val PrimaryPurpleGradient = Brush.horizontalGradient(
    colors = listOf(PurpleGradientStart, PurpleGradientEnd)
)

val CyanGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF06B6D4), Color(0xFF3B82F6))
)

val CardGlowGradient = Brush.verticalGradient(
    colors = listOf(PrimaryPurple.copy(alpha = 0.12f), Color.Transparent)
)
