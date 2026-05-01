package com.csu.schedule

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.csu.schedule.ui.screen.Screen
import com.csu.schedule.ui.screen.`import`.ImportScreen
import com.csu.schedule.ui.screen.weekgrid.WeekGridScreen
import com.csu.schedule.ui.theme.CSUScheduleTheme
import com.csu.schedule.ui.viewmodel.ScheduleViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CSUScheduleTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ScheduleApp()
                }
            }
        }
    }
}

@Composable
fun ScheduleApp(viewModel: ScheduleViewModel = viewModel()) {
    Crossfade(
        targetState = viewModel.currentScreen,
        label = "screen"
    ) { screen ->
        when (screen) {
            Screen.WeekGrid -> WeekGridScreen(
                semester = viewModel.semester,
                courses = viewModel.filteredCourses,
                selectedWeek = viewModel.selectedWeek,
                actualWeek = viewModel.actualWeek,
                showWeekend = viewModel.showWeekend,
                importState = viewModel.importState,
                onImportClick = { viewModel.navigateTo(Screen.Import) },
                onPreviousWeek = viewModel::previousWeek,
                onNextWeek = viewModel::nextWeek,
                onJumpToCurrentWeek = viewModel::jumpToCurrentWeek,
                onCourseClick = { viewModel.selectCourse(it) },
                onImportStateConsumed = viewModel::resetImportState,
                selectedCourse = viewModel.selectedCourse,
                onDismissDetail = { viewModel.selectCourse(null) }
            )

            Screen.Import -> ImportScreen(
                importState = viewModel.importState,
                onBack = { viewModel.navigateTo(Screen.WeekGrid) },
                onImport = { uri, startDate ->
                    viewModel.importFile(uri, startDate)
                }
            )
        }
    }
}
