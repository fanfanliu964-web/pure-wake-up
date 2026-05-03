package com.csu.schedule.ui.screen.weekgrid

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.csu.schedule.data.db.CourseEntity
import com.csu.schedule.ui.theme.CourseColors
import com.csu.schedule.ui.theme.CourseColorsDark

@Composable
fun CourseCard(
    course: CourseEntity,
    isCurrent: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val palette = if (isSystemInDarkTheme()) CourseColorsDark else CourseColors
    val color = palette[course.colorIndex % palette.size]
    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(1.dp)
            .clip(shape)
            .background(color.background)
            .then(
                if (isCurrent) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, shape)
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 5.dp)
    ) {
        Column {
            Text(
                text = course.courseName,
                style = MaterialTheme.typography.labelSmall,
                color = color.text,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (course.classroom.isNotBlank()) {
                Text(
                    text = course.classroom,
                    style = MaterialTheme.typography.labelSmall,
                    color = color.text.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
