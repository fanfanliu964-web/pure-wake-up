package com.csu.schedule.ui.screen.weekgrid

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.csu.schedule.data.db.CourseEntity
import com.csu.schedule.ui.theme.CourseColors
import com.csu.schedule.ui.theme.CourseColorsDark
import com.csu.schedule.util.TimeSlots

private val DAY_NAMES = listOf("", "周一", "周二", "周三", "周四", "周五", "周六", "周日")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailSheet(
    course: CourseEntity,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val palette = if (isSystemInDarkTheme()) CourseColorsDark else CourseColors
    val color = palette[course.colorIndex % palette.size]
    val timeRange = TimeSlots.rangeFor(course.startSection, course.endSection)
    val dayName = DAY_NAMES.getOrElse(course.dayOfWeek) { "" }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = color.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = course.courseName,
                style = MaterialTheme.typography.headlineMedium,
                color = color.text
            )

            Spacer(modifier = Modifier.height(16.dp))

            DetailRow("上课时间", "$dayName 第${course.startSection}-${course.endSection}节 ${timeRange.startTime}-${timeRange.endTime}", color.text)
            DetailRow("上课地点", course.classroom.ifBlank { "未指定" }, color.text)
            DetailRow("上课周次", course.weekPattern.ifBlank { "未指定" }, color.text)
            DetailRow("班级", course.classGroup.ifBlank { "未指定" }, color.text)
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, textColor: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor.copy(alpha = 0.6f),
            modifier = Modifier.width(72.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor
        )
    }
}
