package com.csu.schedule.data.`import`

import com.csu.schedule.data.db.CourseEntity
import com.csu.schedule.data.db.SemesterEntity

object ImportDiffCalculator {
    fun compare(
        previewCourses: List<PreviewCourse>,
        existingSemester: SemesterEntity?,
        existingCourses: List<CourseEntity>
    ): ImportDiffSummary {
        if (existingSemester == null) {
            return ImportDiffSummary(newCount = previewCourses.size)
        }

        val incoming = previewCourses.associateBy { it.diffKey() }
        val existing = existingCourses.associateBy { it.diffKey() }

        var changed = 0
        var unchanged = 0
        for ((key, course) in incoming) {
            val old = existing[key]
            when {
                old == null -> {}
                old.weekPattern == course.weekPattern &&
                    old.classroom == course.classroom &&
                    old.classGroup == course.classGroup &&
                    old.totalHours == course.totalHours -> unchanged++
                else -> changed++
            }
        }

        return ImportDiffSummary(
            matchingSemesterId = existingSemester.id,
            newCount = incoming.keys.count { it !in existing.keys },
            changedCount = changed,
            unchangedCount = unchanged,
            removedCount = existing.keys.count { it !in incoming.keys }
        )
    }

    private fun PreviewCourse.diffKey(): String =
        "${dayOfWeek}|${startSection}|${endSection}|${courseName.trim()}"

    private fun CourseEntity.diffKey(): String =
        "${dayOfWeek}|${startSection}|${endSection}|${courseName.trim()}"
}
