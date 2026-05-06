package com.csu.schedule.data.`import`

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CourseMapperTest {

    @Test
    fun `maps empty classroom without shifting class group`() {
        val grid = emptyGrid()
        grid[1][2] = """
            应用化学综合实验
            4-12周(64学时)

            应化2305
        """.trimIndent()

        val courses = CourseMapper.map(
            RawSchedule(studentName = "刘一凡", semesterName = "2025-2026-2", grid = grid),
            semesterId = 7L
        )

        assertEquals(1, courses.size)
        assertEquals(7L, courses[0].semesterId)
        assertEquals(2, courses[0].dayOfWeek)
        assertEquals(5, courses[0].startSection)
        assertEquals(6, courses[0].endSection)
        assertEquals("", courses[0].classroom)
        assertEquals("应化2305", courses[0].classGroup)
    }

    @Test
    fun `preview reports course and unique counts`() {
        val grid = emptyGrid()
        grid[0][1] = """
            专业英语
            1-6,8周(32学时)
            B座118
            应化2304-05
        """.trimIndent()
        grid[2][2] = """
            专业英语
            7周(32学时)
            B座118
            应化2304-05
        """.trimIndent()

        val preview = CourseMapper.preview(
            RawSchedule(studentName = "刘一凡", semesterName = "2025-2026-2", grid = grid)
        )

        assertEquals("2025-2026-2", preview.semesterName)
        assertEquals(2, preview.courseCount)
        assertEquals(1, preview.uniqueCourseCount)
        assertEquals("2026-02-23", preview.suggestedStartDate)
        assertEquals(20, preview.totalWeeks)
        assertTrue(preview.warnings.isEmpty())
    }

    private fun emptyGrid(): Array<Array<String>> = Array(7) { Array(6) { "" } }
}
