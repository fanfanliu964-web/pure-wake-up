package com.csu.schedule.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "courses",
    indices = [Index(value = ["semester_id"])]
)
data class CourseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "semester_id")
    val semesterId: Long,
    @ColumnInfo(name = "day_of_week")
    val dayOfWeek: Int,
    @ColumnInfo(name = "start_section")
    val startSection: Int,
    @ColumnInfo(name = "end_section")
    val endSection: Int,
    @ColumnInfo(name = "course_name")
    val courseName: String,
    val classroom: String,
    @ColumnInfo(name = "class_group")
    val classGroup: String,
    @ColumnInfo(name = "week_pattern")
    val weekPattern: String,
    @ColumnInfo(name = "total_hours")
    val totalHours: Int,
    @ColumnInfo(name = "color_index")
    val colorIndex: Int
)
