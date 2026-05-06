package com.csu.schedule.data.`import`

object WeekPatternParser {

    fun parse(pattern: String): Set<Int> {
        if (pattern.isBlank()) return emptySet()

        var cleaned = pattern
            .replace(Regex("[（(].*?[）)]"), "")
            .replace("，", ",")
            .replace("、", ",")
            .replace("－", "-")
            .replace("—", "-")
            .replace("–", "-")
            .replace("第", "")
            .trim()

        val oddOnly = cleaned.contains("单")
        val evenOnly = cleaned.contains("双")
        cleaned = cleaned.replace("单", "").replace("双", "")

        cleaned = cleaned.replace("周", "").trim()

        if (cleaned.isEmpty()) return emptySet()

        val weeks = mutableSetOf<Int>()
        val segments = cleaned.split(",")

        for (segment in segments) {
            val trimmed = segment.trim()
            if (trimmed.isEmpty()) continue

            if (trimmed.contains("-")) {
                val parts = trimmed.split("-")
                val start = parts[0].trim().toIntOrNull() ?: continue
                val end = parts[1].trim().toIntOrNull() ?: continue
                for (w in start..end) {
                    weeks.add(w)
                }
            } else {
                val w = trimmed.toIntOrNull() ?: continue
                weeks.add(w)
            }
        }

        return when {
            oddOnly -> weeks.filter { it % 2 == 1 }.toSet()
            evenOnly -> weeks.filter { it % 2 == 0 }.toSet()
            else -> weeks
        }
    }

    fun parseOrAll(pattern: String, totalWeeks: Int): Set<Int> {
        val parsed = parse(pattern)
        return parsed.ifEmpty { (1..totalWeeks).toSet() }
    }

    fun containsWeek(pattern: String, week: Int, totalWeeks: Int): Boolean {
        return parseOrAll(pattern, totalWeeks).contains(week)
    }
}
