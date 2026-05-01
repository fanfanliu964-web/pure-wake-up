package com.csu.schedule.ui.screen.weekgrid

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.csu.schedule.data.db.CourseEntity
import com.csu.schedule.data.db.SemesterEntity
import com.csu.schedule.ui.viewmodel.ImportState
import com.csu.schedule.util.WeekCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekGridScreen(
    semester: SemesterEntity?,
    courses: List<CourseEntity>,
    selectedWeek: Int,
    actualWeek: Int,
    showWeekend: Boolean,
    importState: ImportState,
    onImportClick: () -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onJumpToCurrentWeek: () -> Unit,
    onCourseClick: (CourseEntity) -> Unit,
    onImportStateConsumed: () -> Unit,
    selectedCourse: CourseEntity?,
    onDismissDetail: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(importState) {
        when (importState) {
            is ImportState.Success -> {
                snackbarHostState.showSnackbar("成功导入 ${importState.courseCount} 门课程")
                onImportStateConsumed()
            }
            is ImportState.Error -> {
                snackbarHostState.showSnackbar(importState.message)
                onImportStateConsumed()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 8.dp, top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "我的课表",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onImportClick) {
                    Icon(
                        Icons.Default.FileOpen,
                        contentDescription = "导入课表",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (semester != null) {
                // Week selector
                WeekSelector(
                    currentWeek = selectedWeek,
                    isCurrentWeek = selectedWeek == actualWeek,
                    onPreviousWeek = onPreviousWeek,
                    onNextWeek = onNextWeek,
                    onJumpToCurrentWeek = onJumpToCurrentWeek
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Grid
                WeekGrid(
                    courses = courses,
                    todayDayOfWeek = if (selectedWeek == actualWeek) WeekCalculator.todayDayOfWeek() else 0,
                    showWeekend = showWeekend,
                    onCourseClick = onCourseClick,
                    modifier = Modifier.weight(1f)
                )
            } else {
                // Empty state
                EmptyState(onImportClick = onImportClick)
            }
        }

        if (selectedCourse != null) {
            CourseDetailSheet(
                course = selectedCourse,
                onDismiss = onDismissDetail
            )
        }
    }
}

@Composable
private fun EmptyState(onImportClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Text(
            text = "还没有课表",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "从教务系统下载课表 (.xls)，然后导入到这里",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        androidx.compose.material3.FilledTonalButton(onClick = onImportClick) {
            Icon(Icons.Default.FileOpen, contentDescription = null)
            Spacer(modifier = Modifier.padding(4.dp))
            Text("导入课表")
        }
    }
}
