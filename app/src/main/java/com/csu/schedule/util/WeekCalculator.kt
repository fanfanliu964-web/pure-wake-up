package com.csu.schedule.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object WeekCalculator {
    fun currentWeek(semesterStartDate: String, today: LocalDate = LocalDate.now()): Int? {
        val start = runCatching {
            LocalDate.parse(semesterStartDate, DateTimeFormatter.ISO_LOCAL_DATE)
        }.getOrNull() ?: return null
        val daysBetween = ChronoUnit.DAYS.between(start, today)
        if (daysBetween < 0) return null
        return (daysBetween / 7).toInt() + 1
    }

    fun todayDayOfWeek(): Int {
        return when (LocalDate.now().dayOfWeek) {
            DayOfWeek.MONDAY -> 1
            DayOfWeek.TUESDAY -> 2
            DayOfWeek.WEDNESDAY -> 3
            DayOfWeek.THURSDAY -> 4
            DayOfWeek.FRIDAY -> 5
            DayOfWeek.SATURDAY -> 6
            DayOfWeek.SUNDAY -> 7
        }
    }

    fun weekStartDate(semesterStartDate: String, weekNumber: Int): LocalDate {
        val start = LocalDate.parse(semesterStartDate, DateTimeFormatter.ISO_LOCAL_DATE)
        return start.plusWeeks((weekNumber - 1).toLong())
    }

    fun weekDateRange(semesterStartDate: String, weekNumber: Int): String {
        val mon = weekStartDate(semesterStartDate, weekNumber)
        val sun = mon.plusDays(6)
        val fmt = DateTimeFormatter.ofPattern("M/d")
        return "${mon.format(fmt)}-${sun.format(fmt)}"
    }
}
