package com.csu.schedule.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
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

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF0A84FF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1A2E4A),
    onPrimaryContainer = Color(0xFF90B8F0),
    secondary = Color(0xFF8E8E93),
    onSecondary = Color.White,
    background = Color(0xFF000000),
    onBackground = Color(0xFFE5E5EA),
    surface = Color(0xFF1C1C1E),
    onSurface = Color(0xFFE5E5EA),
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = Color(0xFF8E8E93),
    outline = Color(0xFF48484A),
)

@Composable
fun CSUScheduleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
