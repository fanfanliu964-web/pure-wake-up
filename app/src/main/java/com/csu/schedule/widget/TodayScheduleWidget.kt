package com.csu.schedule.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import com.csu.schedule.data.db.AppDatabase
import com.csu.schedule.data.`import`.WeekPatternParser
import com.csu.schedule.util.WeekCalculator

class TodayScheduleWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = AppDatabase.getInstance(context)
        val semester = db.semesterDao().getActiveSemesterSync()
        val dayOfWeek = WeekCalculator.todayDayOfWeek()

        val weekNumber = semester?.let {
            WeekCalculator.currentWeek(it.startDate)
        } ?: 1

        val courses = semester?.let {
            db.courseDao().getCoursesByDaySync(it.id, dayOfWeek)
                .filter { course ->
                    WeekPatternParser.parse(course.weekPattern).contains(weekNumber)
                }
                .sortedBy { course -> course.startSection }
        } ?: emptyList()

        provideContent {
            GlanceTheme {
                WidgetContent(
                    weekNumber = weekNumber,
                    dayOfWeek = dayOfWeek,
                    courses = courses
                )
            }
        }
    }
}
