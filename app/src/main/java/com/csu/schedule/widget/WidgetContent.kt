package com.csu.schedule.widget

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.csu.schedule.data.db.CourseEntity
import com.csu.schedule.util.TimeSlots

private val DAY_NAMES = listOf("", "周一", "周二", "周三", "周四", "周五", "周六", "周日")

@Composable
fun WidgetContent(
    weekNumber: Int,
    dayOfWeek: Int,
    courses: List<CourseEntity>,
    status: WidgetCourseStatus,
    hasSchedule: Boolean,
    intentForCourse: (Long) -> Intent
) {
    val dayName = DAY_NAMES.getOrElse(dayOfWeek) { "" }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(12.dp)
    ) {
        Text(
            text = if (hasSchedule && !status.isOutOfTerm) "今日课程 · 第${weekNumber}周 · $dayName" else "今日课程",
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = GlanceTheme.colors.onSurface
            )
        )

        Spacer(modifier = GlanceModifier.height(8.dp))

        if (status.course != null) {
            HighlightCourse(status, intentForCourse)
            Spacer(modifier = GlanceModifier.height(6.dp))
        } else if (!hasSchedule || status.isOutOfTerm || courses.isEmpty()) {
            EmptyWidgetMessage(status.label)
            return@Column
        } else {
            Text(
                text = status.label,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
            Spacer(modifier = GlanceModifier.height(6.dp))
        }

        courses.take(4).forEach { course ->
            WidgetCourseItem(course, intentForCourse(course.id))
            Spacer(modifier = GlanceModifier.height(4.dp))
        }
    }
}

@Composable
private fun HighlightCourse(
    status: WidgetCourseStatus,
    intentForCourse: (Long) -> Intent
) {
    val course = status.course ?: return
    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(GlanceTheme.colors.primaryContainer)
            .clickable(actionStartActivity(intentForCourse(course.id)))
            .padding(8.dp)
    ) {
        Text(
            text = status.label,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = GlanceTheme.colors.onPrimaryContainer
            )
        )
        Text(
            text = course.courseName,
            style = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = GlanceTheme.colors.onPrimaryContainer
            )
        )
        val timeRange = TimeSlots.rangeFor(course.startSection, course.endSection)
        Text(
            text = "${timeRange.startTime}-${timeRange.endTime}"
                + if (course.classroom.isNotBlank()) " · ${course.classroom}" else "",
            style = TextStyle(
                fontSize = 12.sp,
                color = GlanceTheme.colors.onPrimaryContainer
            )
        )
        if (status.detail.isNotBlank()) {
            Text(
                text = status.detail,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = GlanceTheme.colors.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
private fun EmptyWidgetMessage(message: String) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = message,
            style = TextStyle(
                fontSize = 15.sp,
                color = GlanceTheme.colors.onSurfaceVariant
            )
        )
    }
}

@Composable
private fun WidgetCourseItem(course: CourseEntity, intent: Intent) {
    val timeRange = TimeSlots.rangeFor(course.startSection, course.endSection)

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .clickable(actionStartActivity(intent))
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = timeRange.startTime,
            style = TextStyle(
                fontSize = 12.sp,
                color = GlanceTheme.colors.onSurfaceVariant
            ),
            modifier = GlanceModifier.width(44.dp)
        )

        Column {
            Text(
                text = course.courseName,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = GlanceTheme.colors.onSurface
                )
            )
            if (course.classroom.isNotBlank()) {
                Text(
                    text = course.classroom,
                    style = TextStyle(
                        fontSize = 11.sp,
                        color = GlanceTheme.colors.onSurfaceVariant
                    )
                )
            }
        }
    }
}
