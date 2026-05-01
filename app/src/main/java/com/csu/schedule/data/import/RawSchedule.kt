package com.csu.schedule.data.`import`

data class RawSchedule(
    val studentName: String,
    val semesterName: String,
    val grid: Array<Array<String>>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RawSchedule) return false
        return studentName == other.studentName &&
            semesterName == other.semesterName &&
            grid.contentDeepEquals(other.grid)
    }

    override fun hashCode(): Int {
        var result = studentName.hashCode()
        result = 31 * result + semesterName.hashCode()
        result = 31 * result + grid.contentDeepHashCode()
        return result
    }
}
