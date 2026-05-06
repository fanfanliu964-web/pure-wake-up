package com.csu.schedule.ui.viewmodel

data class CourseFormInput(
    val courseName: String,
    val dayOfWeek: Int,
    val startSection: Int,
    val endSection: Int,
    val weekPattern: String,
    val classroom: String,
    val classGroup: String,
    val totalHours: Int
)

object CourseFormValidator {
    fun validate(input: CourseFormInput): String? {
        return when {
            input.courseName.isBlank() -> "课程名不能为空"
            input.dayOfWeek !in 1..7 -> "星期必须在 1-7 之间"
            input.startSection !in 1..12 || input.endSection !in 1..12 -> "节次必须在 1-12 之间"
            input.startSection > input.endSection -> "开始节次不能晚于结束节次"
            input.weekPattern.isBlank() -> "周次不能为空"
            input.totalHours < 0 -> "学时不能为负数"
            else -> null
        }
    }
}
