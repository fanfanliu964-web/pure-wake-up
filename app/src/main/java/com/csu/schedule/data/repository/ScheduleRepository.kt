package com.csu.schedule.data.repository

import androidx.room.withTransaction
import com.csu.schedule.data.db.AppDatabase
import com.csu.schedule.data.db.CourseEntity
import com.csu.schedule.data.db.SemesterEntity
import com.csu.schedule.data.`import`.CourseMapper
import com.csu.schedule.data.`import`.ImportDiffCalculator
import com.csu.schedule.data.`import`.ImportPreview
import com.csu.schedule.data.`import`.WeekPatternParser
import com.csu.schedule.data.`import`.XlsReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.InputStream

class ScheduleRepository(private val db: AppDatabase) {

    fun getAllSemesters(): Flow<List<SemesterEntity>> =
        db.semesterDao().getAllSemesters()

    fun getActiveSemester(): Flow<SemesterEntity?> =
        db.semesterDao().getActiveSemester()

    fun getCoursesBySemester(semesterId: Long): Flow<List<CourseEntity>> =
        db.courseDao().getCoursesBySemester(semesterId)

    suspend fun getActiveSemesterSync(): SemesterEntity? =
        db.semesterDao().getActiveSemesterSync()

    suspend fun getCourseById(courseId: Long): CourseEntity? = withContext(Dispatchers.IO) {
        db.courseDao().getCourseByIdSync(courseId)
    }

    suspend fun getCoursesByDaySync(semesterId: Long, day: Int): List<CourseEntity> =
        db.courseDao().getCoursesByDaySync(semesterId, day)

    suspend fun previewXls(
        inputStream: InputStream,
        totalWeeks: Int = 20
    ): Result<ImportPreview> = withContext(Dispatchers.IO) {
        runCatching {
            val raw = XlsReader.read(inputStream)
            val basePreview = CourseMapper.preview(raw, totalWeeks)
            val existing = db.semesterDao().getSemesterByNameSync(basePreview.semesterName)
            val existingCourses = existing?.let {
                db.courseDao().getCoursesBySemesterSync(it.id)
            }.orEmpty()
            CourseMapper.preview(
                raw = raw,
                totalWeeks = totalWeeks,
                diffSummary = ImportDiffCalculator.compare(basePreview.courses, existing, existingCourses)
            )
        }
    }

    suspend fun importPreview(
        preview: ImportPreview,
        startDate: String,
        totalWeeks: Int = preview.totalWeeks,
        replaceMatching: Boolean = true
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            if (replaceMatching && preview.diffSummary.matchingSemesterId != null) {
                replaceSemester(preview, startDate, totalWeeks, preview.diffSummary.matchingSemesterId)
            } else {
                insertPreview(preview, startDate, totalWeeks)
            }
        }
    }

    suspend fun importFromXls(
        inputStream: InputStream,
        startDate: String,
        totalWeeks: Int = 20
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val preview = CourseMapper.preview(XlsReader.read(inputStream), totalWeeks)
            insertPreview(preview, startDate, totalWeeks)
        }
    }

    suspend fun activateSemester(semesterId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            db.semesterDao().activateOnly(semesterId)
        }
    }

    suspend fun deleteSemester(semesterId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            db.withTransaction {
                val deletedSemester = db.semesterDao().getSemesterByIdSync(semesterId)
                db.courseDao().deleteBySemester(semesterId)
                db.semesterDao().deleteById(semesterId)
                if (deletedSemester?.isActive == true) {
                    db.semesterDao().getLatestExceptSync(semesterId)?.let { replacement ->
                        db.semesterDao().activateOnly(replacement.id)
                    }
                }
            }
        }
    }

    suspend fun saveCourse(course: CourseEntity): Result<Long> = withContext(Dispatchers.IO) {
        runCatching {
            if (course.id == 0L) {
                db.courseDao().insert(course.copy(colorIndex = CourseMapper.colorIndexFor(course.courseName)))
            } else {
                db.courseDao().update(course.copy(colorIndex = CourseMapper.colorIndexFor(course.courseName)))
                course.id
            }
        }
    }

    suspend fun deleteCourse(courseId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            db.courseDao().deleteById(courseId)
        }
    }

    fun getWeeksForCourse(weekPattern: String): Set<Int> =
        WeekPatternParser.parse(weekPattern)

    fun coursesForWeek(
        courses: List<CourseEntity>,
        week: Int,
        totalWeeks: Int = 20
    ): List<CourseEntity> =
        courses.filter { WeekPatternParser.containsWeek(it.weekPattern, week, totalWeeks) }

    fun filterCoursesForWeek(
        courses: List<CourseEntity>,
        week: Int,
        totalWeeks: Int = 20
    ): List<CourseEntity> = coursesForWeek(courses, week, totalWeeks)

    private suspend fun insertPreview(
        preview: ImportPreview,
        startDate: String,
        totalWeeks: Int
    ): Int = db.withTransaction {
        db.semesterDao().deactivateAll()
        val semesterId = db.semesterDao().insert(
            SemesterEntity(
                name = preview.semesterName,
                startDate = startDate,
                totalWeeks = totalWeeks,
                isActive = true
            )
        )
        insertPreviewCourses(preview, semesterId)
    }

    private suspend fun replaceSemester(
        preview: ImportPreview,
        startDate: String,
        totalWeeks: Int,
        semesterId: Long
    ): Int = db.withTransaction {
        db.semesterDao().deactivateAll()
        db.semesterDao().update(
            SemesterEntity(
                id = semesterId,
                name = preview.semesterName,
                startDate = startDate,
                totalWeeks = totalWeeks,
                isActive = true
            )
        )
        db.courseDao().deleteBySemester(semesterId)
        insertPreviewCourses(preview, semesterId)
    }

    private suspend fun insertPreviewCourses(preview: ImportPreview, semesterId: Long): Int {
        val courses = preview.courses.map { course ->
            CourseEntity(
                semesterId = semesterId,
                dayOfWeek = course.dayOfWeek,
                startSection = course.startSection,
                endSection = course.endSection,
                courseName = course.courseName,
                classroom = course.classroom,
                classGroup = course.classGroup,
                weekPattern = course.weekPattern,
                totalHours = course.totalHours,
                colorIndex = course.colorIndex
            )
        }
        db.courseDao().insertAll(courses)
        return courses.size
    }

}
