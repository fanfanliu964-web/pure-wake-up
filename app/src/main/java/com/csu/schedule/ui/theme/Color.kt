package com.csu.schedule.ui.theme

import androidx.compose.ui.graphics.Color

data class CourseColor(val background: Color, val text: Color)

val CourseColors = listOf(
    CourseColor(Color(0xFFD4E4FF), Color(0xFF2D5DAA)),
    CourseColor(Color(0xFFFFDDD4), Color(0xFFAA4A2D)),
    CourseColor(Color(0xFFD4FFD6), Color(0xFF2D8A33)),
    CourseColor(Color(0xFFFFF4D4), Color(0xFF8A7A2D)),
    CourseColor(Color(0xFFE8D4FF), Color(0xFF6B2DAA)),
    CourseColor(Color(0xFFD4FFF4), Color(0xFF2D8A7A)),
    CourseColor(Color(0xFFFFD4E8), Color(0xFFAA2D6B)),
    CourseColor(Color(0xFFFFE8D4), Color(0xFFAA6B2D)),
    CourseColor(Color(0xFFD4EEFF), Color(0xFF2D7AAA)),
    CourseColor(Color(0xFFF0D4FF), Color(0xFF7A2DAA)),
)

val CourseColorsDark = listOf(
    CourseColor(Color(0xFF1A2E4A), Color(0xFF90B8F0)),
    CourseColor(Color(0xFF4A2418), Color(0xFFF09080)),
    CourseColor(Color(0xFF1A3A1E), Color(0xFF78D47A)),
    CourseColor(Color(0xFF3A3010), Color(0xFFD4B857)),
    CourseColor(Color(0xFF2E1A4A), Color(0xFFB87AF0)),
    CourseColor(Color(0xFF1A3A33), Color(0xFF78D4C0)),
    CourseColor(Color(0xFF4A1830), Color(0xFFF078B0)),
    CourseColor(Color(0xFF4A3018), Color(0xFFF0B078)),
    CourseColor(Color(0xFF1A304A), Color(0xFF78B4F0)),
    CourseColor(Color(0xFF3A1A4A), Color(0xFFC078F0)),
)

// Light
val SurfaceLight = Color(0xFFF5F5F7)
val OnSurfaceLight = Color(0xFF1D1D1F)
val SecondaryTextLight = Color(0xFF86868B)
val DividerLight = Color(0xFFE5E5EA)
val TodayHighlight = Color(0xFFF0F4FF)

// Dark
val SurfaceDark = Color(0xFF1C1C1E)
val OnSurfaceDark = Color(0xFFE5E5EA)
val SecondaryTextDark = Color(0xFF8E8E93)
val DividerDark = Color(0xFF38383A)
val TodayHighlightDark = Color(0xFF1A2540)
