package com.csu.schedule.ui.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CourseFormValidatorTest {

    @Test
    fun `valid course form passes`() {
        assertNull(CourseFormValidator.validate(validInput()))
    }

    @Test
    fun `blank course name fails`() {
        assertEquals(
            "课程名不能为空",
            CourseFormValidator.validate(validInput(courseName = " "))
        )
    }

    @Test
    fun `start section cannot be after end section`() {
        assertEquals(
            "开始节次不能晚于结束节次",
            CourseFormValidator.validate(validInput(startSection = 5, endSection = 4))
        )
    }

    private fun validInput(
        courseName: String = "专业英语",
        startSection: Int = 1,
        endSection: Int = 2
    ) = CourseFormInput(
        courseName = courseName,
        dayOfWeek = 1,
        startSection = startSection,
        endSection = endSection,
        weekPattern = "1-16周",
        classroom = "B座118",
        classGroup = "应化2304",
        totalHours = 32
    )
}
