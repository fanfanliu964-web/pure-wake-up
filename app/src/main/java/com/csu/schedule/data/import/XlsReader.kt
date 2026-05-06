package com.csu.schedule.data.`import`

import jxl.Workbook
import jxl.Sheet
import java.io.InputStream

object XlsReader {

    fun read(inputStream: InputStream): RawSchedule {
        val workbook = Workbook.getWorkbook(inputStream)
        try {
            require(workbook.numberOfSheets > 0) {
                "无法解析此文件，是否为 CSU 课程表？"
            }
            val sheet = workbook.getSheet(0)

            validateShape(sheet)

            val header = contentsAt(sheet, 0, 0)
            require(header.contains("中南大学") && header.contains("学生课表")) {
                "无法解析此文件，是否为 CSU 课程表？"
            }

            val studentName = extractStudentName(header)
            val semesterName = extractSemesterName(contentsAt(sheet, 0, 1))
            require(semesterName.isNotBlank()) {
                "无法解析学期信息，请确认导出的 CSU .xls 文件格式"
            }

            val slotColumns = findSlotColumns(sheet)
            require(slotColumns.size == SLOT_LABELS.size) {
                "无法识别完整节次列，请确认导出的 CSU .xls 文件格式"
            }
            validateDayRows(sheet)

            val grid = Array(7) { dayIndex ->
                Array(6) { slotIndex ->
                    val row = dayIndex + 3
                    val col = slotColumns[slotIndex]
                    contentsAt(sheet, col, row).trim()
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

    private fun findSlotColumns(sheet: Sheet): List<Int> {
        val columns = mutableListOf<Int>()
        val row = 2

        for (col in 0 until sheet.columns) {
            val content = contentsAt(sheet, col, row).trim()
            if (content.isEmpty()) continue

            val normalized = content.replace("－", "-")
            if (normalized in SLOT_LABELS) {
                columns.add(col)
            }
        }

        return columns
    }

    private fun validateShape(sheet: Sheet) {
        require(sheet.rows >= 10 && sheet.columns >= 7) {
            "无法解析此文件，是否为 CSU 课程表？"
        }
    }

    private fun validateDayRows(sheet: Sheet) {
        for ((index, dayName) in DAY_LABELS.withIndex()) {
            val rowLabel = contentsAt(sheet, 0, index + 3)
            require(rowLabel.contains(dayName)) {
                "无法识别完整星期行，请确认导出的 CSU .xls 文件格式"
            }
        }
    }

    private fun contentsAt(sheet: Sheet, col: Int, row: Int): String {
        return if (col in 0 until sheet.columns && row in 0 until sheet.rows) {
            sheet.getCell(col, row).contents
        } else {
            ""
        }
    }

    private val SLOT_LABELS = listOf("1-2", "3-4", "5-6", "7-8", "9-10", "11-12")
    private val DAY_LABELS = listOf("星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日")
}
