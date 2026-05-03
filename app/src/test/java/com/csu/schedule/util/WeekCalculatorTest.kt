package com.csu.schedule.util

import org.junit.Assert.assertEquals
import org.junit.Test

class WeekCalculatorTest {
    @Test
    fun weekDateRangeUsesDotSeparatedMonthDay() {
        assertEquals("3.9-3.15", WeekCalculator.weekDateRange("2026-03-09", 1))
    }
}
