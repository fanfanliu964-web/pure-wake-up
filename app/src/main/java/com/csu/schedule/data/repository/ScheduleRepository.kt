package com.csu.schedule.data.repository

import com.csu.schedule.data.db.AppDatabase
import com.csu.schedule.data.db.CourseEntity
import com.csu.schedule.data.db.SemesterEntity
import com.csu.schedule.data.`import`.CourseMapper
import com.csu.schedule.data.`import`.WeekPatternParser
import com.csu.schedule.data.`import`.XlsReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.InputStream

class ScheduleRepository(private val db: AppDatabase) {

    fun getActiveSemester(): Flow<SemesterEntity?> =
        db.semesterDao().getActiveSemester()

    fun getCoursesBySemester(semesterId: Long): Flow<List<CourseEntity>> =
        db.courseDao().getCoursesBySemester(semesterId)

    suspend fun getActiveSemesterSync(): SemesterEntity? =
        db.semesterDao().getActiveSemesterSync()

    suspend fun getCoursesByDaySync(semesterId: Long, day: Int): List<CourseEntity> =
        db.courseDao().getCoursesByDaySync(semesterId, day)

    suspend fun importFromXls(
        inputStream: InputStream,
        startDate: String,
        totalWeeks: Int = 20
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val raw = XlsReader.read(inputStream)

            db.semesterDao().deactivateAll()

            val semesterId = db.semesterDao().insert(
                SemesterEntity(
                    name = raw.semesterName,
                    startDate = startDate,
                    totalWeeks = totalWeeks,
                    isActive = true
                )
            )

            val courses = CourseMapper.map(raw, semesterId)
            db.courseDao().insertAll(courses)
            courses.size
        }
    }

    fun getWeeksForCourse(weekPattern: String): Set<Int> =
        WeekPatternParser.parse(weekPattern)

    fun filterCoursesForWeek(courses: List<CourseEntity>, week: Int): List<CourseEntity> =
        courses.filter { WeekPatternParser.parse(it.weekPattern).contains(week) }
}
