package com.csu.schedule.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import com.csu.schedule.EXTRA_COURSE_ID
import com.csu.schedule.MainActivity
import com.csu.schedule.data.db.AppDatabase
import com.csu.schedule.data.`import`.WeekPatternParser
import com.csu.schedule.util.WeekCalculator

class TodayScheduleWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = AppDatabase.getInstance(context)
        val semester = db.semesterDao().getActiveSemesterSync()
        val dayOfWeek = WeekCalculator.todayDayOfWeek()
        val weekNumber = semester?.let { WeekCalculator.currentWeek(it.startDate) } ?: 1

        val courses = semester?.let {
            db.courseDao().getCoursesByDaySync(it.id, dayOfWeek)
                .filter { course ->
                    WeekPatternParser.containsWeek(course.weekPattern, weekNumber, it.totalWeeks)
                }
                .sortedBy { course -> course.startSection }
        }.orEmpty()

        val status = NextCourseCalculator.status(
            hasSchedule = semester != null,
            semesterStartDate = semester?.startDate,
            totalWeeks = semester?.totalWeeks ?: 20,
            courses = courses
        )

        provideContent {
            GlanceTheme {
                WidgetContent(
                    weekNumber = weekNumber,
                    dayOfWeek = dayOfWeek,
                    courses = courses,
                    status = status,
                    hasSchedule = semester != null,
                    intentForCourse = { courseId -> courseIntent(context, courseId) }
                )
            }
        }
    }

    private fun courseIntent(context: Context, courseId: Long): Intent {
        return Intent(context, MainActivity::class.java)
            .putExtra(EXTRA_COURSE_ID, courseId)
            .setData(Uri.parse("purewakeup://course/$courseId"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }
}
