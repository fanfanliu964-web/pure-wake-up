package com.csu.schedule.data.`import`

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class XlsReaderSampleTest {

    @Test
    fun `reads bundled csu sample schedule`() {
        val sample = listOf(
            File("学生个人课表_8202230711.xls"),
            File("../学生个人课表_8202230711.xls")
        ).first { it.isFile }

        val raw = sample.inputStream().use { stream ->
            XlsReader.read(stream)
        }
        val preview = CourseMapper.preview(raw)

        assertEquals("2025-2026-2", raw.semesterName)
        assertEquals(23, preview.courseCount)
        assertEquals(9, preview.uniqueCourseCount)
        assertTrue(
            preview.courses.any {
                it.courseName == "应用化学综合实验" &&
                    it.classroom.isBlank() &&
                    it.classGroup == "应化2305"
            }
        )
    }
}
