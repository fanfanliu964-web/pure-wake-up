package com.csu.schedule.ui.screen.weekgrid

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.csu.schedule.data.db.CourseEntity
import com.csu.schedule.ui.theme.DividerDark
import com.csu.schedule.ui.theme.DividerLight
import com.csu.schedule.ui.theme.SecondaryTextDark
import com.csu.schedule.ui.theme.SecondaryTextLight
import com.csu.schedule.ui.theme.TodayHighlight
import com.csu.schedule.ui.theme.TodayHighlightDark
import com.csu.schedule.util.CurrentSlot
import com.csu.schedule.util.TimeSlots
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val TIME_COLUMN_WIDTH = 52.dp
private val HEADER_HEIGHT = 60.dp

private val DAY_LABELS = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
private val SLOT_LABELS = listOf("1-2", "3-4", "5-6", "7-8", "9-10", "11-12")
private val DATE_FMT = DateTimeFormatter.ofPattern("M.d")

@Composable
fun WeekGrid(
    courses: List<CourseEntity>,
    todayDayOfWeek: Int,
    onCourseClick: (CourseEntity) -> Unit,
    onSlotCoursesClick: (List<CourseEntity>) -> Unit,
    modifier: Modifier = Modifier,
    weekStartDate: LocalDate? = null,
    currentSlot: CurrentSlot? = null
) {
    val dayCount = 7
    val courseMap = buildCourseMap(courses)
    val isDark = isSystemInDarkTheme()
    val dividerColor = if (isDark) DividerDark else DividerLight
    val secondaryText = if (isDark) SecondaryTextDark else SecondaryTextLight
    val todayBg = if (isDark) TodayHighlightDark else TodayHighlight

    Column(modifier = modifier.fillMaxSize()) {
        // Day header row — fixed height
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(TIME_COLUMN_WIDTH)
                    .height(HEADER_HEIGHT),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "节次",
                    style = MaterialTheme.typography.labelSmall,
                    color = secondaryText
                )
            }

            for (day in 1..dayCount) {
                val isToday = day == todayDayOfWeek
                val dateLabel = weekStartDate
                    ?.plusDays((day - 1).toLong())
                    ?.format(DATE_FMT) ?: ""

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(HEADER_HEIGHT)
                        .background(if (isToday) todayBg else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = DAY_LABELS[day - 1],
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                            color = if (isToday) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        if (dateLabel.isNotEmpty()) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = dateLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                else secondaryText,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = dividerColor)

        // Slot rows — weight(1f) fills remaining height evenly, no rounding gap
        for ((slotIndex, slotLabel) in SLOT_LABELS.withIndex()) {
            val slotPair = TimeSlots.slotPairs[slotIndex]
            val timeRange = TimeSlots.rangeFor(slotPair.first, slotPair.second)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Time label column
                Box(
                    modifier = Modifier
                        .width(TIME_COLUMN_WIDTH)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = slotLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = secondaryText
                        )
                        Text(
                            text = timeRange.startTime,
                            style = MaterialTheme.typography.labelSmall,
                            color = secondaryText.copy(alpha = 0.6f)
                        )
                    }
                }

                // Day cells
                for (day in 1..dayCount) {
                    val isToday = day == todayDayOfWeek
                    val key = day to slotPair.first
                    val coursesInSlot = courseMap[key] ?: emptyList()

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(if (isToday) todayBg else Color.Transparent)
                    ) {
                        if (coursesInSlot.isNotEmpty()) {
                            val course = coursesInSlot.first()
                            val isCurrent = currentSlot != null &&
                                currentSlot.section != null &&
                                day == currentSlot.dayOfWeek &&
                                currentSlot.section in course.startSection..course.endSection
                            CourseCard(
                                course = course,
                                isCurrent = isCurrent,
                                modifier = Modifier.fillMaxSize(),
                                onClick = {
                                    if (coursesInSlot.size == 1) {
                                        onCourseClick(course)
                                    } else {
                                        onSlotCoursesClick(coursesInSlot)
                                    }
                                }
                            )
                            if (coursesInSlot.size > 1) {
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(3.dp),
                                    shape = RoundedCornerShape(99.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Text(
                                        text = "+${coursesInSlot.size - 1}",
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = dividerColor)
        }
    }
}

private fun buildCourseMap(courses: List<CourseEntity>): Map<Pair<Int, Int>, List<CourseEntity>> {
    return courses.groupBy { it.dayOfWeek to it.startSection }
}
