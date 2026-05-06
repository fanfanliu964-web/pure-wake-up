package com.csu.schedule.data.`import`

import com.csu.schedule.data.db.CourseEntity
import com.csu.schedule.data.db.SemesterEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class ImportDiffCalculatorTest {

    @Test
    fun `reports new changed unchanged and removed courses`() {
        val semester = SemesterEntity(
            id = 3L,
            name = "2025-2026-2",
            startDate = "2026-02-23",
            totalWeeks = 20,
            isActive = true
        )
        val existing = listOf(
            course(name = "专业英语", classroom = "B座118"),
            course(name = "无机化学", classroom = "B座201")
        )
        val incoming = listOf(
            preview(name = "专业英语", classroom = "B座118"),
            preview(name = "无机化学", classroom = "B座202"),
            preview(name = "物质分离", classroom = "B座208")
        )

        val diff = ImportDiffCalculator.compare(incoming, semester, existing)

        assertEquals(3L, diff.matchingSemesterId)
        assertEquals(1, diff.newCount)
        assertEquals(1, diff.changedCount)
        assertEquals(1, diff.unchangedCount)
        assertEquals(0, diff.removedCount)
    }

    @Test
    fun `without matching semester all incoming courses are new`() {
        val diff = ImportDiffCalculator.compare(
            previewCourses = listOf(preview("专业英语"), preview("物质分离")),
            existingSemester = null,
            existingCourses = emptyList()
        )

        assertEquals(null, diff.matchingSemesterId)
        assertEquals(2, diff.newCount)
    }

    private fun preview(
        name: String,
        classroom: String = "B座118"
    ) = PreviewCourse(
        dayOfWeek = 1,
        startSection = 1,
        endSection = 2,
        courseName = name,
        classroom = classroom,
        classGroup = "应化2304",
        weekPattern = "1-16周",
        totalHours = 32,
        colorIndex = 0
    )

    private fun course(
        name: String,
        classroom: String
    ) = CourseEntity(
        id = 0L,
        semesterId = 3L,
        dayOfWeek = 1,
        startSection = 1,
        endSection = 2,
        courseName = name,
        classroom = classroom,
        classGroup = "应化2304",
        weekPattern = "1-16周",
        totalHours = 32,
        colorIndex = 0
    )
}
