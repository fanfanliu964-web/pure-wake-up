package com.csu.schedule.ui.screen

sealed interface Screen {
    data object WeekGrid : Screen
    data object Import : Screen
    data object Manage : Screen
    data object CourseEditor : Screen
}
