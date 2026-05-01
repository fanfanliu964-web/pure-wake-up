package com.csu.schedule

import android.app.Application
import com.csu.schedule.data.db.AppDatabase
import com.csu.schedule.widget.WidgetUpdateWorker

class App : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        WidgetUpdateWorker.schedule(this)
    }
}
