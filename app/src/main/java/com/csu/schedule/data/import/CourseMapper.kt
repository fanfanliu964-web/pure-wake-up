package com.csu.schedule.data.`import`

import com.csu.schedule.data.db.CourseEntity

object CourseMapper {

    private const val PALETTE_SIZE = 10

    fun map(raw: RawSchedule, semesterId: Long): List<CourseEntity> {
        val colorMap = mutableMapOf<String, Int>()
        var nextColor = 0
        val entities = mutableListOf<CourseEntity>()

        val slotSections = listOf(1 to 2, 3 to 4, 5 to 6, 7 to 8, 9 to 10, 11 to 12)

        for (dayIndex in 0 until 7) {
            for (slotIndex in 0 until 6) {
                val cellText = raw.grid.getOrNull(dayIndex)?.getOrNull(slotIndex) ?: continue
                val courses = CellParser.parse(cellText)

                for (course in courses) {
                    val colorIndex = colorMap.getOrPut(course.courseName) {
                        (nextColor++ % PALETTE_SIZE)
                    }

                    val (startSection, endSection) = slotSections[slotIndex]

                    entities.add(
                        CourseEntity(
                            semesterId = semesterId,
                            dayOfWeek = dayIndex + 1,
                            startSection = startSection,
                            endSection = endSection,
                            courseName = course.courseName,
                            classroom = course.classroom,
                            classGroup = course.classGroup,
                            weekPattern = course.weekPattern,
                            totalHours = course.totalHours,
                            colorIndex = colorIndex
                        )
                    )
                }
            }
        }

        return entities
    }
}
