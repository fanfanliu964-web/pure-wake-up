package com.csu.schedule.ui.screen.weekgrid

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.csu.schedule.data.db.CourseEntity
import com.csu.schedule.data.db.SemesterEntity
import com.csu.schedule.ui.viewmodel.ImportState
import com.csu.schedule.util.WeekCalculator
import com.csu.schedule.util.rememberCurrentSlot
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekGridScreen(
    semester: SemesterEntity?,
    actualWeek: Int,
    importState: ImportState,
    onImportClick: () -> Unit,
    getCoursesForWeek: (Int) -> List<CourseEntity>,
    onCourseClick: (CourseEntity) -> Unit,
    onImportStateConsumed: () -> Unit,
    selectedCourse: CourseEntity?,
    onDismissDetail: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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

    if (semester == null) {
        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
            EmptyState(
                onImportClick = onImportClick,
                modifier = Modifier.padding(padding)
            )
        }
        return
    }

    val totalWeeks = semester.totalWeeks
    val pagerState = rememberPagerState(
        initialPage = (actualWeek - 1).coerceIn(0, totalWeeks - 1)
    ) { totalWeeks }

    val currentSlot by rememberCurrentSlot()

    val displayWeek = pagerState.currentPage + 1

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (displayWeek != actualWeek) {
                SmallFloatingActionButton(
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(actualWeek - 1) }
                    }
                ) {
                    Icon(Icons.Default.Today, contentDescription = "回本周")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            CompactTopBar(
                weekNumber = displayWeek,
                dateRange = WeekCalculator.weekDateRange(semester.startDate, displayWeek),
                isCurrentWeek = displayWeek == actualWeek,
                onImportClick = onImportClick
            )

            HorizontalPager(
                state = pagerState,
                beyondBoundsPageCount = 2,
                modifier = Modifier.weight(1f)
            ) { page ->
                val weekNumber = page + 1
                val courses = remember(weekNumber) { getCoursesForWeek(weekNumber) }
                val weekStart = remember(weekNumber) { WeekCalculator.weekStartDate(semester.startDate, weekNumber) }

                WeekGrid(
                    courses = courses,
                    todayDayOfWeek = if (weekNumber == actualWeek) WeekCalculator.todayDayOfWeek() else 0,
                    onCourseClick = onCourseClick,
                    weekStartDate = weekStart,
                    currentSlot = if (weekNumber == actualWeek) currentSlot else null,
                    modifier = Modifier.fillMaxSize()
                )
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
private fun EmptyState(
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Text(
            text = "还没有课表",
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "从教务系统下载课表 (.xls)，然后导入到这里",
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        androidx.compose.material3.FilledTonalButton(onClick = onImportClick) {
            Icon(Icons.Default.FileOpen, contentDescription = null)
            Spacer(modifier = Modifier.padding(4.dp))
            Text("导入课表")
        }
    }
}
