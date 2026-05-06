package com.csu.schedule.data.`import`

data class ImportPreview(
    val semesterName: String,
    val courseCount: Int,
    val uniqueCourseCount: Int,
    val courses: List<PreviewCourse>,
    val warnings: List<String>,
    val suggestedStartDate: String,
    val totalWeeks: Int,
    val diffSummary: ImportDiffSummary = ImportDiffSummary()
)

data class PreviewCourse(
    val dayOfWeek: Int,
    val startSection: Int,
    val endSection: Int,
    val courseName: String,
    val classroom: String,
    val classGroup: String,
    val weekPattern: String,
    val totalHours: Int,
    val colorIndex: Int
)

data class ImportDiffSummary(
    val matchingSemesterId: Long? = null,
    val newCount: Int = 0,
    val changedCount: Int = 0,
    val unchangedCount: Int = 0,
    val removedCount: Int = 0
) {
    val hasMatch: Boolean get() = matchingSemesterId != null
    val hasChanges: Boolean
        get() = newCount > 0 || changedCount > 0 || removedCount > 0
}
