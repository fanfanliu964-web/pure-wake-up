package com.csu.schedule.data.`import`

import org.junit.Assert.assertEquals
import org.junit.Test

class CellParserTest {

    @Test
    fun `keeps empty classroom line before class group`() {
        val courses = CellParser.parse(
            """
            应用化学综合实验
            4-12周(64学时)

            应化2305
            """.trimIndent()
        )

        assertEquals(1, courses.size)
        assertEquals("应用化学综合实验", courses[0].courseName)
        assertEquals("4-12周(64学时)", courses[0].weekPattern)
        assertEquals("", courses[0].classroom)
        assertEquals("应化2305", courses[0].classGroup)
        assertEquals(64, courses[0].totalHours)
    }

    @Test
    fun `splits multiple courses in one slot`() {
        val courses = CellParser.parse(
            """
            物质分离原理与技术
            6-8周(12学时)
            B座208
            应化2301-02
            ---------
            物质分离原理与技术
            3周(2学时)
            B座208
            应化2301-02
            """.trimIndent()
        )

        assertEquals(2, courses.size)
        assertEquals("6-8周(12学时)", courses[0].weekPattern)
        assertEquals("3周(2学时)", courses[1].weekPattern)
    }

    @Test
    fun `missing optional fields stay blank`() {
        val courses = CellParser.parse("形势与政策")

        assertEquals(1, courses.size)
        assertEquals("形势与政策", courses[0].courseName)
        assertEquals("", courses[0].weekPattern)
        assertEquals("", courses[0].classroom)
        assertEquals("", courses[0].classGroup)
        assertEquals(0, courses[0].totalHours)
    }

    @Test
    fun `extracts hours from full width parentheses`() {
        val courses = CellParser.parse(
            """
            专业英语
            1-6周（32学时）
            B座118
            应化2304-05
            """.trimIndent()
        )

        assertEquals(32, courses[0].totalHours)
    }
}
