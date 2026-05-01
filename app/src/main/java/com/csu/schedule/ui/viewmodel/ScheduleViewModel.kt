package com.csu.schedule.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.csu.schedule.App
import com.csu.schedule.data.db.CourseEntity
import com.csu.schedule.data.db.SemesterEntity
import com.csu.schedule.data.repository.ScheduleRepository
import com.csu.schedule.ui.screen.Screen
import com.csu.schedule.util.WeekCalculator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ScheduleRepository(
        (application as App).database
    )

    var currentScreen by mutableStateOf<Screen>(Screen.WeekGrid)
        private set

    var semester by mutableStateOf<SemesterEntity?>(null)
        private set

    var allCourses by mutableStateOf<List<CourseEntity>>(emptyList())
        private set

    var filteredCourses by mutableStateOf<List<CourseEntity>>(emptyList())
        private set

    var selectedWeek by mutableIntStateOf(1)
        private set

    var actualWeek by mutableIntStateOf(1)
        private set

    var selectedCourse by mutableStateOf<CourseEntity?>(null)
        private set

    var importState by mutableStateOf<ImportState>(ImportState.Idle)
        private set

    var showWeekend by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            repository.getActiveSemester()
                .filterNotNull()
                .flatMapLatest { sem ->
                    semester = sem
                    val week = WeekCalculator.currentWeek(sem.startDate) ?: 1
                    actualWeek = week
                    selectedWeek = week
                    repository.getCoursesBySemester(sem.id)
                }
                .collectLatest { courses ->
                    allCourses = courses
                    updateFilteredCourses()
                    checkWeekendCourses()
                }
        }
    }

    fun navigateTo(screen: Screen) {
        currentScreen = screen
    }

    fun selectWeek(week: Int) {
        selectedWeek = week.coerceIn(1, semester?.totalWeeks ?: 20)
        updateFilteredCourses()
    }

    fun jumpToCurrentWeek() {
        selectWeek(actualWeek)
    }

    fun previousWeek() {
        selectWeek(selectedWeek - 1)
    }

    fun nextWeek() {
        selectWeek(selectedWeek + 1)
    }

    fun selectCourse(course: CourseEntity?) {
        selectedCourse = course
    }

    fun importFile(uri: Uri, startDate: String) {
        viewModelScope.launch {
            importState = ImportState.Loading
            val context = getApplication<App>()
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    importState = ImportState.Error("无法打开文件")
                    return@launch
                }
                inputStream.use { stream ->
                    val result = repository.importFromXls(stream, startDate)
                    result.fold(
                        onSuccess = { count ->
                            importState = ImportState.Success(count)
                            currentScreen = Screen.WeekGrid
                        },
                        onFailure = { e ->
                            importState = ImportState.Error(
                                when {
                                    e is jxl.read.biff.BiffException ->
                                        "此文件不是有效的课程表"
                                    else ->
                                        "导入失败: ${e.message ?: "未知错误"}"
                                }
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                importState = ImportState.Error("导入失败: ${e.message ?: "未知错误"}")
            }
        }
    }

    fun resetImportState() {
        importState = ImportState.Idle
    }

    private fun updateFilteredCourses() {
        filteredCourses = repository.filterCoursesForWeek(allCourses, selectedWeek)
    }

    private fun checkWeekendCourses() {
        showWeekend = allCourses.any { it.dayOfWeek > 5 }
    }
}

sealed interface ImportState {
    data object Idle : ImportState
    data object Loading : ImportState
    data class Success(val courseCount: Int) : ImportState
    data class Error(val message: String) : ImportState
}
