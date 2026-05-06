package com.csu.schedule.data.`import`

import com.csu.schedule.data.db.CourseEntity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object CourseMapper {

    private const val PALETTE_SIZE = 10
    private val SLOT_SECTIONS = listOf(1 to 2, 3 to 4, 5 to 6, 7 to 8, 9 to 10, 11 to 12)
    private val SEMESTER_REGEX = Regex("(\\d{4})-(\\d{4})-(\\d+)")

    fun map(raw: RawSchedule, semesterId: Long): List<CourseEntity> {
        return previewCourses(raw).map { course ->
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
    }

    fun preview(
        raw: RawSchedule,
        totalWeeks: Int = 20,
        diffSummary: ImportDiffSummary = ImportDiffSummary()
    ): ImportPreview {
        val courses = previewCourses(raw)
        return ImportPreview(
            semesterName = raw.semesterName,
            courseCount = courses.size,
            uniqueCourseCount = courses.map { it.courseName }.distinct().size,
            courses = courses,
            warnings = buildWarnings(courses, totalWeeks),
            suggestedStartDate = suggestStartDate(raw.semesterName),
            totalWeeks = totalWeeks,
            diffSummary = diffSummary
        )
    }

    fun colorIndexFor(courseName: String): Int =
        (courseName.trim().hashCode() and Int.MAX_VALUE) % PALETTE_SIZE

    private fun previewCourses(raw: RawSchedule): List<PreviewCourse> {
        val colorMap = mutableMapOf<String, Int>()
        var nextColor = 0
        val courses = mutableListOf<PreviewCourse>()

        for (dayIndex in 0 until 7) {
            for (slotIndex in 0 until 6) {
                val cellText = raw.grid.getOrNull(dayIndex)?.getOrNull(slotIndex) ?: continue
                for (course in CellParser.parse(cellText)) {
                    val colorIndex = colorMap.getOrPut(course.courseName) {
                        nextColor++ % PALETTE_SIZE
                    }
                    val (startSection, endSection) = SLOT_SECTIONS[slotIndex]

                    courses.add(
                        PreviewCourse(
                            dayOfWeek = dayIndex + 1,
                            startSection = startSection,
                            endSection = endSection,
                            courseName = course.courseName,
                            classroom = course.classroom,
                            classGroup = course.classGroup,
                            weekPattern = course.weekPattern,
                            totalHours = course.totalHours,
                            colorIndex = colorIndex
                        )
                    )
                }
            }
        }

        return courses
    }

    private fun buildWarnings(courses: List<PreviewCourse>, totalWeeks: Int): List<String> {
        val invalidWeekPatterns = courses
            .filter { it.weekPattern.isBlank() || WeekPatternParser.parse(it.weekPattern).isEmpty() }
            .map { it.courseName }
            .distinct()

        return if (invalidWeekPatterns.isEmpty()) {
            emptyList()
        } else {
            listOf(
                "有 ${invalidWeekPatterns.size} 门课程周次为空或无法解析，将默认显示在第 1-${totalWeeks} 周",
                "异常课程: ${invalidWeekPatterns.take(6).joinToString("、")}"
                    + if (invalidWeekPatterns.size > 6) " 等" else ""
            )
        }
    }

    private fun suggestStartDate(semesterName: String): String {
        val match = SEMESTER_REGEX.find(semesterName)
        val now = LocalDate.now()
        if (match == null) return seasonalDefault(now)

        val firstYear = match.groupValues[1].toIntOrNull() ?: return seasonalDefault(now)
        val secondYear = match.groupValues[2].toIntOrNull() ?: return seasonalDefault(now)
        val term = match.groupValues[3].toIntOrNull() ?: return seasonalDefault(now)

        val anchor = if (term == 1) {
            LocalDate.of(firstYear, 9, 1)
        } else {
            LocalDate.of(secondYear, 2, 24)
        }
        return firstMondayOnOrBefore(anchor).format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    private fun seasonalDefault(now: LocalDate): String {
        val anchor = if (now.monthValue >= 8) {
            LocalDate.of(now.year, 9, 1)
        } else {
            LocalDate.of(now.year, 2, 24)
        }
        return firstMondayOnOrBefore(anchor).format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    private fun firstMondayOnOrBefore(date: LocalDate): LocalDate {
        var candidate = date
        while (candidate.dayOfWeek != DayOfWeek.MONDAY) {
            candidate = candidate.minusDays(1)
        }
        return candidate
    }
}
