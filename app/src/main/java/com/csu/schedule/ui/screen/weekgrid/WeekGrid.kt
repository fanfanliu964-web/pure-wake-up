package com.csu.schedule.ui.screen.weekgrid

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.csu.schedule.data.db.CourseEntity
import com.csu.schedule.ui.theme.DividerLight
import com.csu.schedule.ui.theme.SecondaryTextLight
import com.csu.schedule.ui.theme.TodayHighlight
import com.csu.schedule.util.TimeSlots

private val TIME_COLUMN_WIDTH = 44.dp
private val DAY_COLUMN_MIN_WIDTH = 56.dp
private val ROW_HEIGHT = 80.dp
private val HEADER_HEIGHT = 48.dp

private val DAY_LABELS = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
private val SLOT_LABELS = listOf("1-2", "3-4", "5-6", "7-8", "9-10", "11-12")

@Composable
fun WeekGrid(
    courses: List<CourseEntity>,
    todayDayOfWeek: Int,
    showWeekend: Boolean,
    onCourseClick: (CourseEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val dayCount = if (showWeekend) 7 else 5
    val courseMap = buildCourseMap(courses)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Day header row
        Row(modifier = Modifier.fillMaxWidth()) {
            // Time column header
            Box(
                modifier = Modifier
                    .width(TIME_COLUMN_WIDTH)
                    .height(HEADER_HEIGHT),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "时间",
                    style = MaterialTheme.typography.labelSmall,
                    color = SecondaryTextLight
                )
            }

            for (day in 1..dayCount) {
                val isToday = day == todayDayOfWeek
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(HEADER_HEIGHT)
                        .then(
                            if (isToday) Modifier.background(TodayHighlight)
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = DAY_LABELS[day - 1],
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isToday) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        HorizontalDivider(color = DividerLight)

        // Grid rows
        for ((slotIndex, slotLabel) in SLOT_LABELS.withIndex()) {
            val slotPair = TimeSlots.slotPairs[slotIndex]
            val timeRange = TimeSlots.rangeFor(slotPair.first, slotPair.second)

            Row(modifier = Modifier.fillMaxWidth()) {
                // Time label
                Box(
                    modifier = Modifier
                        .width(TIME_COLUMN_WIDTH)
                        .height(ROW_HEIGHT),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = slotLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = SecondaryTextLight
                        )
                        Text(
                            text = timeRange.startTime,
                            style = MaterialTheme.typography.labelSmall,
                            color = SecondaryTextLight.copy(alpha = 0.6f)
                        )
                    }
                }

                for (day in 1..dayCount) {
                    val isToday = day == todayDayOfWeek
                    val key = day to slotPair.first
                    val coursesInSlot = courseMap[key] ?: emptyList()

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(ROW_HEIGHT)
                            .then(
                                if (isToday) Modifier.background(TodayHighlight)
                                else Modifier
                            )
                    ) {
                        if (coursesInSlot.isNotEmpty()) {
                            CourseCard(
                                course = coursesInSlot.first(),
                                onClick = { onCourseClick(coursesInSlot.first()) }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = DividerLight)
        }
    }
}

private fun buildCourseMap(courses: List<CourseEntity>): Map<Pair<Int, Int>, List<CourseEntity>> {
    return courses.groupBy { it.dayOfWeek to it.startSection }
}
