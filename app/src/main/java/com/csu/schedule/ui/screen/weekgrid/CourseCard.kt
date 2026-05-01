package com.csu.schedule.ui.screen.weekgrid

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.csu.schedule.data.db.CourseEntity
import com.csu.schedule.ui.theme.CourseColors

@Composable
fun CourseCard(
    course: CourseEntity,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val color = CourseColors[course.colorIndex % CourseColors.size]

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(1.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color.background)
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Column {
            Text(
                text = course.courseName,
                style = MaterialTheme.typography.labelSmall,
                color = color.text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            )
            if (course.classroom.isNotBlank()) {
                Text(
                    text = course.classroom,
                    style = MaterialTheme.typography.labelSmall,
                    color = color.text.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
