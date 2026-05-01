package com.csu.schedule.util

data class TimeRange(val startTime: String, val endTime: String)

object TimeSlots {
    val schedule = mapOf(
        1 to TimeRange("08:00", "08:45"),
        2 to TimeRange("08:55", "09:40"),
        3 to TimeRange("10:00", "10:45"),
        4 to TimeRange("10:55", "11:40"),
        5 to TimeRange("14:00", "14:45"),
        6 to TimeRange("14:55", "15:40"),
        7 to TimeRange("16:00", "16:45"),
        8 to TimeRange("16:55", "17:40"),
        9 to TimeRange("19:00", "19:45"),
        10 to TimeRange("19:55", "20:40"),
        11 to TimeRange("20:50", "21:35"),
        12 to TimeRange("21:45", "22:30"),
    )

    fun rangeFor(startSection: Int, endSection: Int) = TimeRange(
        startTime = schedule[startSection]?.startTime ?: "",
        endTime = schedule[endSection]?.endTime ?: ""
    )

    val slotPairs = listOf(1 to 2, 3 to 4, 5 to 6, 7 to 8, 9 to 10, 11 to 12)
}
