package com.csu.schedule.data.`import`

import jxl.Workbook
import java.io.InputStream

object XlsReader {

    fun read(inputStream: InputStream): RawSchedule {
        val workbook = Workbook.getWorkbook(inputStream)
        try {
            val sheet = workbook.getSheet(0)

            val studentName = extractStudentName(sheet.getCell(0, 0).contents)
            val semesterName = extractSemesterName(sheet.getCell(0, 1).contents)

            val slotColumns = findSlotColumns(sheet)

            val grid = Array(7) { dayIndex ->
                Array(6) { slotIndex ->
                    val row = dayIndex + 3
                    val col = slotColumns.getOrNull(slotIndex) ?: return@Array ""
                    sheet.getCell(col, row).contents.trim()
                }
            }

            return RawSchedule(studentName, semesterName, grid)
        } finally {
            workbook.close()
        }
    }

    private fun extractStudentName(header: String): String {
        val trimmed = header.replace("中南大学", "").replace("学生课表", "").trim()
        return trimmed
    }

    private fun extractSemesterName(info: String): String {
        val regex = Regex("学年学期[：:]\\s*(\\S+)")
        return regex.find(info)?.groupValues?.get(1) ?: ""
    }

    private fun findSlotColumns(sheet: jxl.Sheet): List<Int> {
        val slotHeaders = listOf("1－2", "1-2", "3－4", "3-4", "5－6", "5-6", "7－8", "7-8", "9－10", "9-10", "11－12", "11-12")
        val columns = mutableListOf<Int>()
        val row = 2

        for (col in 0 until sheet.columns) {
            val content = sheet.getCell(col, row).contents.trim()
            if (content.isEmpty()) continue

            val normalized = content.replace("－", "-")
            if (normalized in listOf("1-2", "3-4", "5-6", "7-8", "9-10", "11-12")) {
                columns.add(col)
            }
        }

        return columns
    }
}
