package com.csu.schedule.widget

import com.csu.schedule.data.db.CourseEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class NextCourseCalculatorTest {

    @Test
    fun `returns current course while class is running`() {
        val status = NextCourseCalculator.status(
            hasSchedule = true,
            semesterStartDate = "2026-03-09",
            totalWeeks = 20,
            courses = listOf(course(start = 1, end = 2)),
            today = LocalDate.of(2026, 3, 9),
            now = LocalTime.of(8, 30)
        )

        assertEquals("正在上课", status.label)
        assertEquals("专业英语", status.course?.courseName)
    }

    @Test
    fun `returns next course before class starts`() {
        val status = NextCourseCalculator.status(
            hasSchedule = true,
            semesterStartDate = "2026-03-09",
            totalWeeks = 20,
            courses = listOf(course(start = 3, end = 4)),
            today = LocalDate.of(2026, 3, 9),
            now = LocalTime.of(9, 45)
        )

        assertEquals("下一节课", status.label)
        assertEquals("专业英语", status.course?.courseName)
    }

    @Test
    fun `returns out of term`() {
        val status = NextCourseCalculator.status(
            hasSchedule = true,
            semesterStartDate = "2026-03-09",
            totalWeeks = 1,
            courses = listOf(course(start = 1, end = 2)),
            today = LocalDate.of(2026, 4, 1),
            now = LocalTime.of(8, 30)
        )

        assertEquals("不在学期内", status.label)
    }

    private fun course(start: Int, end: Int) = CourseEntity(
        id = 7L,
        semesterId = 1L,
        dayOfWeek = 1,
        startSection = start,
        endSection = end,
        courseName = "专业英语",
        classroom = "B座118",
        classGroup = "应化2304",
        weekPattern = "1-16周",
        totalHours = 32,
        colorIndex = 0
    )
}
