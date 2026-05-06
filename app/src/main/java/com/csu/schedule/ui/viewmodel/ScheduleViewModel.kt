package com.csu.schedule.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.csu.schedule.App
import com.csu.schedule.data.db.CourseEntity
import com.csu.schedule.data.db.SemesterEntity
import com.csu.schedule.data.`import`.CourseMapper
import com.csu.schedule.data.`import`.ImportPreview
import com.csu.schedule.data.repository.ScheduleRepository
import com.csu.schedule.ui.screen.Screen
import com.csu.schedule.util.WeekCalculator
import com.csu.schedule.widget.TodayScheduleWidget
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ScheduleRepository((application as App).database)
    private var pendingCourseId: Long? = null

    var uiState by mutableStateOf(ScheduleUiState())
        private set

    init {
        viewModelScope.launch {
            repository.getAllSemesters().collectLatest { semesters ->
                uiState = uiState.copy(semesters = semesters)
            }
        }

        viewModelScope.launch {
            repository.getActiveSemester()
                .flatMapLatest { semester ->
                    val actualWeek = semester?.let {
                        WeekCalculator.currentWeek(it.startDate)?.coerceIn(1, it.totalWeeks) ?: 1
                    } ?: 1
                    uiState = uiState.copy(
                        activeSemester = semester,
                        allCourses = emptyList(),
                        weekCourseMap = emptyMap(),
                        actualWeek = actualWeek,
                        isInitialLoadComplete = semester == null
                    )
                    if (semester == null) {
                        flowOf(emptyList())
                    } else {
                        repository.getCoursesBySemester(semester.id)
                    }
                }
                .collectLatest { courses ->
                    val semester = uiState.activeSemester
                    val total = semester?.totalWeeks ?: 20
                    val weekCourseMap = (1..total).associateWith { week ->
                        repository.coursesForWeek(courses, week, total)
                    }
                    uiState = uiState.copy(
                        allCourses = courses.sortedWith(compareBy<CourseEntity> { it.dayOfWeek }.thenBy { it.startSection }),
                        weekCourseMap = weekCourseMap,
                        isInitialLoadComplete = true
                    )
                    openPendingCourseIfReady()
                }
        }
    }

    fun navigateTo(screen: Screen) {
        uiState = uiState.copy(currentScreen = screen)
    }

    fun startImport() {
        uiState = uiState.copy(importState = ImportState.Idle, currentScreen = Screen.Import)
    }

    fun startManage() {
        uiState = uiState.copy(currentScreen = Screen.Manage)
    }

    fun startCourseEditor(course: CourseEntity? = null) {
        val activeSemesterId = uiState.activeSemester?.id ?: return
        uiState = uiState.copy(
            currentScreen = Screen.CourseEditor,
            editor = CourseEditorState.fromCourse(course, activeSemesterId)
        )
    }

    fun updateEditor(editor: CourseEditorState) {
        uiState = uiState.copy(editor = editor.copy(error = null))
    }

    fun selectCourse(course: CourseEntity?) {
        uiState = uiState.copy(selectedCourse = course)
    }

    fun selectSlotCourses(courses: List<CourseEntity>) {
        uiState = uiState.copy(slotCourses = courses)
    }

    fun previewFile(uri: Uri) {
        viewModelScope.launch {
            uiState = uiState.copy(importState = ImportState.Loading("正在解析课表..."))
            val context = getApplication<App>()
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    uiState = uiState.copy(importState = ImportState.Error("无法打开文件"))
                    return@launch
                }
                inputStream.use { stream ->
                    val result = repository.previewXls(stream)
                    result.fold(
                        onSuccess = { preview ->
                            uiState = uiState.copy(importState = ImportState.PreviewReady(preview))
                        },
                        onFailure = { e ->
                            uiState = uiState.copy(importState = ImportState.Error(messageForImportError(e)))
                        }
                    )
                }
            } catch (e: Exception) {
                uiState = uiState.copy(importState = ImportState.Error(messageForImportError(e)))
            }
        }
    }

    fun importPreview(startDate: String, totalWeeks: Int, replaceMatching: Boolean) {
        val preview = (uiState.importState as? ImportState.PreviewReady)?.preview
        if (preview == null) {
            uiState = uiState.copy(importState = ImportState.Error("请先选择并预览课表文件"))
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(importState = ImportState.Loading("正在写入课表..."))
            val result = repository.importPreview(preview, startDate, totalWeeks, replaceMatching)
            result.fold(
                onSuccess = { count ->
                    refreshWidget()
                    uiState = uiState.copy(
                        importState = ImportState.Success(count),
                        currentScreen = Screen.WeekGrid
                    )
                },
                onFailure = { e ->
                    uiState = uiState.copy(importState = ImportState.Error(messageForImportError(e)))
                }
            )
        }
    }

    fun activateSemester(semesterId: Long) {
        viewModelScope.launch {
            repository.activateSemester(semesterId).fold(
                onSuccess = {
                    refreshWidget()
                    uiState = uiState.copy(message = "已切换学期")
                },
                onFailure = { uiState = uiState.copy(message = "切换失败: ${it.message ?: "未知错误"}") }
            )
        }
    }

    fun deleteSemester(semesterId: Long) {
        viewModelScope.launch {
            repository.deleteSemester(semesterId).fold(
                onSuccess = {
                    refreshWidget()
                    uiState = uiState.copy(message = "已删除学期")
                },
                onFailure = { uiState = uiState.copy(message = "删除失败: ${it.message ?: "未知错误"}") }
            )
        }
    }

    fun saveEditor() {
        val editor = uiState.editor ?: return
        val parsed = editor.toInput()
        val error = parsed.error ?: CourseFormValidator.validate(parsed.input!!)
        if (error != null) {
            uiState = uiState.copy(editor = editor.copy(error = error))
            return
        }

        val input = parsed.input!!
        val course = CourseEntity(
            id = editor.id ?: 0L,
            semesterId = editor.semesterId,
            dayOfWeek = input.dayOfWeek,
            startSection = input.startSection,
            endSection = input.endSection,
            courseName = input.courseName.trim(),
            classroom = input.classroom.trim(),
            classGroup = input.classGroup.trim(),
            weekPattern = input.weekPattern.trim(),
            totalHours = input.totalHours,
            colorIndex = CourseMapper.colorIndexFor(input.courseName)
        )

        viewModelScope.launch {
            repository.saveCourse(course).fold(
                onSuccess = {
                    refreshWidget()
                    uiState = uiState.copy(
                        currentScreen = Screen.Manage,
                        editor = null,
                        message = "课程已保存"
                    )
                },
                onFailure = { e ->
                    uiState = uiState.copy(editor = editor.copy(error = "保存失败: ${e.message ?: "未知错误"}"))
                }
            )
        }
    }

    fun deleteCourse(courseId: Long) {
        viewModelScope.launch {
            repository.deleteCourse(courseId).fold(
                onSuccess = {
                    refreshWidget()
                    uiState = uiState.copy(message = "课程已删除")
                },
                onFailure = { uiState = uiState.copy(message = "删除失败: ${it.message ?: "未知错误"}") }
            )
        }
    }

    fun openCourseFromIntent(courseId: Long?) {
        if (courseId == null || courseId <= 0) return
        pendingCourseId = courseId
        viewModelScope.launch {
            repository.getCourseById(courseId)?.let { course ->
                pendingCourseId = null
                if (uiState.activeSemester?.id != course.semesterId) {
                    repository.activateSemester(course.semesterId)
                    refreshWidget()
                }
                uiState = uiState.copy(
                    currentScreen = Screen.WeekGrid,
                    selectedCourse = course
                )
            }
        }
    }

    fun resetImportState() {
        uiState = uiState.copy(importState = ImportState.Idle)
    }

    fun consumeMessage() {
        uiState = uiState.copy(message = null)
    }

    fun coursesForWeek(week: Int): List<CourseEntity> =
        uiState.weekCourseMap[week] ?: emptyList()

    private fun openPendingCourseIfReady() {
        val courseId = pendingCourseId ?: return
        val course = uiState.allCourses.firstOrNull { it.id == courseId } ?: return
        pendingCourseId = null
        uiState = uiState.copy(currentScreen = Screen.WeekGrid, selectedCourse = course)
    }

    private suspend fun refreshWidget() {
        TodayScheduleWidget().updateAll(getApplication<App>())
    }

    private fun messageForImportError(e: Throwable): String {
        return when (e) {
            is jxl.read.biff.BiffException -> "此文件不是有效的课程表"
            is IllegalArgumentException -> e.message ?: "无法解析此文件，是否为 CSU 课程表？"
            else -> "导入失败: ${e.message ?: "未知错误"}"
        }
    }
}

data class ScheduleUiState(
    val currentScreen: Screen = Screen.WeekGrid,
    val activeSemester: SemesterEntity? = null,
    val semesters: List<SemesterEntity> = emptyList(),
    val allCourses: List<CourseEntity> = emptyList(),
    val weekCourseMap: Map<Int, List<CourseEntity>> = emptyMap(),
    val actualWeek: Int = 1,
    val selectedCourse: CourseEntity? = null,
    val slotCourses: List<CourseEntity> = emptyList(),
    val importState: ImportState = ImportState.Idle,
    val isInitialLoadComplete: Boolean = false,
    val editor: CourseEditorState? = null,
    val message: String? = null
)

data class CourseEditorState(
    val id: Long? = null,
    val semesterId: Long,
    val courseName: String = "",
    val dayOfWeek: String = "1",
    val startSection: String = "1",
    val endSection: String = "2",
    val weekPattern: String = "1-16周",
    val classroom: String = "",
    val classGroup: String = "",
    val totalHours: String = "0",
    val error: String? = null
) {
    fun toInput(): ParsedCourseInput {
        val input = CourseFormInput(
            courseName = courseName,
            dayOfWeek = dayOfWeek.toIntOrNull() ?: return ParsedCourseInput(error = "星期必须是数字"),
            startSection = startSection.toIntOrNull() ?: return ParsedCourseInput(error = "开始节次必须是数字"),
            endSection = endSection.toIntOrNull() ?: return ParsedCourseInput(error = "结束节次必须是数字"),
            weekPattern = weekPattern,
            classroom = classroom,
            classGroup = classGroup,
            totalHours = totalHours.toIntOrNull() ?: return ParsedCourseInput(error = "学时必须是数字")
        )
        return ParsedCourseInput(input = input)
    }

    companion object {
        fun fromCourse(course: CourseEntity?, activeSemesterId: Long): CourseEditorState {
            return if (course == null) {
                CourseEditorState(semesterId = activeSemesterId)
            } else {
                CourseEditorState(
                    id = course.id,
                    semesterId = course.semesterId,
                    courseName = course.courseName,
                    dayOfWeek = course.dayOfWeek.toString(),
                    startSection = course.startSection.toString(),
                    endSection = course.endSection.toString(),
                    weekPattern = course.weekPattern,
                    classroom = course.classroom,
                    classGroup = course.classGroup,
                    totalHours = course.totalHours.toString()
                )
            }
        }
    }
}

data class ParsedCourseInput(
    val input: CourseFormInput? = null,
    val error: String? = null
)

sealed interface ImportState {
    data object Idle : ImportState
    data class Loading(val message: String) : ImportState
    data class PreviewReady(val preview: ImportPreview) : ImportState
    data class Success(val courseCount: Int) : ImportState
    data class Error(val message: String) : ImportState
}
