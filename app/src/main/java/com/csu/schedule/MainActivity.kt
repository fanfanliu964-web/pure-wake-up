package com.csu.schedule

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.csu.schedule.ui.screen.Screen
import com.csu.schedule.ui.screen.`import`.ImportScreen
import com.csu.schedule.ui.screen.manage.CourseEditScreen
import com.csu.schedule.ui.screen.manage.ManageScreen
import com.csu.schedule.ui.screen.weekgrid.WeekGridScreen
import com.csu.schedule.ui.theme.CSUScheduleTheme
import com.csu.schedule.ui.viewmodel.ScheduleViewModel

const val EXTRA_COURSE_ID = "courseId"

class MainActivity : ComponentActivity() {
    private var deepLinkCourseId by mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deepLinkCourseId = intent.courseIdExtra()
        enableEdgeToEdge()
        setContent {
            CSUScheduleTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ScheduleApp(initialCourseId = deepLinkCourseId)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLinkCourseId = intent.courseIdExtra()
    }
}

@Composable
fun ScheduleApp(
    initialCourseId: Long?,
    viewModel: ScheduleViewModel = viewModel()
) {
    val uiState = viewModel.uiState

    LaunchedEffect(initialCourseId) {
        viewModel.openCourseFromIntent(initialCourseId)
    }

    Crossfade(
        targetState = uiState.currentScreen,
        label = "screen"
    ) { screen ->
        when (screen) {
            Screen.WeekGrid -> WeekGridScreen(
                semester = uiState.activeSemester,
                isInitialLoadComplete = uiState.isInitialLoadComplete,
                actualWeek = uiState.actualWeek,
                importState = uiState.importState,
                message = uiState.message,
                onImportClick = viewModel::startImport,
                onManageClick = viewModel::startManage,
                getCoursesForWeek = { week -> viewModel.coursesForWeek(week) },
                onCourseClick = { viewModel.selectCourse(it) },
                onSlotCoursesClick = { viewModel.selectSlotCourses(it) },
                onImportStateConsumed = viewModel::resetImportState,
                onMessageConsumed = viewModel::consumeMessage,
                selectedCourse = uiState.selectedCourse,
                slotCourses = uiState.slotCourses,
                onDismissDetail = { viewModel.selectCourse(null) }
            )

            Screen.Import -> ImportScreen(
                importState = uiState.importState,
                onBack = { viewModel.navigateTo(Screen.WeekGrid) },
                onPreview = { uri -> viewModel.previewFile(uri) },
                onImport = { startDate, totalWeeks, replaceMatching ->
                    viewModel.importPreview(startDate, totalWeeks, replaceMatching)
                }
            )

            Screen.Manage -> ManageScreen(
                semesters = uiState.semesters,
                activeSemester = uiState.activeSemester,
                courses = uiState.allCourses,
                onBack = { viewModel.navigateTo(Screen.WeekGrid) },
                onActivateSemester = viewModel::activateSemester,
                onDeleteSemester = viewModel::deleteSemester,
                onAddCourse = { viewModel.startCourseEditor() },
                onEditCourse = { viewModel.startCourseEditor(it) },
                onDeleteCourse = viewModel::deleteCourse
            )

            Screen.CourseEditor -> uiState.editor?.let { editor ->
                CourseEditScreen(
                    editor = editor,
                    onBack = { viewModel.navigateTo(Screen.Manage) },
                    onChange = viewModel::updateEditor,
                    onSave = viewModel::saveEditor
                )
            }
        }
    }
}

private fun Intent.courseIdExtra(): Long? {
    val id = getLongExtra(EXTRA_COURSE_ID, -1L)
    return id.takeIf { it > 0 }
}
