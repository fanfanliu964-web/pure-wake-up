package com.csu.schedule.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF007AFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4E4FF),
    onPrimaryContainer = Color(0xFF2D5DAA),
    secondary = Color(0xFF86868B),
    onSecondary = Color.White,
    background = Color.White,
    onBackground = Color(0xFF1D1D1F),
    surface = Color(0xFFF5F5F7),
    onSurface = Color(0xFF1D1D1F),
    surfaceVariant = Color(0xFFE5E5EA),
    onSurfaceVariant = Color(0xFF86868B),
    outline = Color(0xFFD1D1D6),
)

@Composable
fun CSUScheduleTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AppTypography,
        content = content
    )
}
