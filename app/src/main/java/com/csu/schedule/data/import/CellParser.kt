package com.csu.schedule.data.`import`

object CellParser {

    private val SEPARATOR = Regex("-{3,}")
    private val HOURS_REGEX = Regex("[（(](\\d+)学时[）)]")

    fun parse(cellText: String): List<ParsedCourse> {
        if (cellText.isBlank()) return emptyList()

        return cellText.split(SEPARATOR)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .mapNotNull { parseSingleCourse(it) }
    }

    private fun parseSingleCourse(text: String): ParsedCourse? {
        val lines = text.lines()
            .map { it.trim() }
            .dropWhile { it.isBlank() }
            .dropLastWhile { it.isBlank() }
        if (lines.isEmpty()) return null

        val courseName = lines[0]
        if (courseName.isBlank()) return null

        val weekPattern = lines.getOrElse(1) { "" }
        val classroom = lines.getOrElse(2) { "" }
        val classGroup = lines.getOrElse(3) { "" }

        val totalHours = HOURS_REGEX.find(weekPattern)
            ?.groupValues?.get(1)?.toIntOrNull() ?: 0

        return ParsedCourse(
            courseName = courseName,
            weekPattern = weekPattern,
            classroom = classroom,
            classGroup = classGroup,
            totalHours = totalHours
        )
    }
}
