package com.csu.schedule.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class WidgetUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        TodayScheduleWidget().updateAll(applicationContext)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "widget_daily_update"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                1, TimeUnit.DAYS
            )
                .setInitialDelay(delayUntilNextMidnightMillis(), TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        private fun delayUntilNextMidnightMillis(): Long {
            val now = LocalDateTime.now()
            val nextMidnight = LocalDate.now().plusDays(1).atStartOfDay()
            return Duration.between(now, nextMidnight).toMillis().coerceAtLeast(0L)
        }
    }
}
