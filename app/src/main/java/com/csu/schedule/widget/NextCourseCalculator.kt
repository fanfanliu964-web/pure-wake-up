package com.csu.schedule.widget

import com.csu.schedule.data.db.CourseEntity
import com.csu.schedule.util.TimeSlots
import com.csu.schedule.util.WeekCalculator
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class WidgetCourseStatus(
    val label: String,
    val course: CourseEntity? = null,
    val detail: String = "",
    val isOutOfTerm: Boolean = false
)

object NextCourseCalculator {
    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

    fun status(
        hasSchedule: Boolean,
        semesterStartDate: String?,
        totalWeeks: Int,
        courses: List<CourseEntity>,
        today: LocalDate = LocalDate.now(),
        now: LocalTime = LocalTime.now()
    ): WidgetCourseStatus {
        if (!hasSchedule || semesterStartDate == null) {
            return WidgetCourseStatus(label = "未导入课表")
        }

        val week = WeekCalculator.currentWeek(semesterStartDate, today)
        if (week == null || week !in 1..totalWeeks) {
            return WidgetCourseStatus(label = "不在学期内", isOutOfTerm = true)
        }

        val todayCourses = courses
            .filter { it.dayOfWeek == WeekCalculator.dayOfWeek(today) }
            .sortedBy { it.startSection }

        if (todayCourses.isEmpty()) {
            return WidgetCourseStatus(label = "今天没有课")
        }

        val current = todayCourses.firstOrNull { course ->
            val range = TimeSlots.rangeFor(course.startSection, course.endSection)
            val start = LocalTime.parse(range.startTime, timeFmt)
            val end = LocalTime.parse(range.endTime, timeFmt)
            !now.isBefore(start) && !now.isAfter(end)
        }
        if (current != null) {
            val end = LocalTime.parse(TimeSlots.rangeFor(current.startSection, current.endSection).endTime, timeFmt)
            return WidgetCourseStatus(
                label = "正在上课",
                course = current,
                detail = "还有 ${Duration.between(now, end).toMinutes().coerceAtLeast(0)} 分钟下课"
            )
        }

        val next = todayCourses.firstOrNull { course ->
            val start = LocalTime.parse(TimeSlots.rangeFor(course.startSection, course.endSection).startTime, timeFmt)
            now.isBefore(start)
        }

        return if (next != null) {
            val start = LocalTime.parse(TimeSlots.rangeFor(next.startSection, next.endSection).startTime, timeFmt)
            WidgetCourseStatus(
                label = "下一节课",
                course = next,
                detail = "还有 ${Duration.between(now, start).toMinutes().coerceAtLeast(0)} 分钟上课"
            )
        } else {
            WidgetCourseStatus(label = "今天课程已结束")
        }
    }
}
