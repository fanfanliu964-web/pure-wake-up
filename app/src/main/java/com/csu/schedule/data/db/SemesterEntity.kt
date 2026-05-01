package com.csu.schedule.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "semesters")
data class SemesterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "start_date")
    val startDate: String,
    @ColumnInfo(name = "total_weeks")
    val totalWeeks: Int,
    @ColumnInfo(name = "is_active")
    val isActive: Boolean
)
