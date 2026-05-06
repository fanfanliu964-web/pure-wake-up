package com.csu.schedule.data.`import`

import org.junit.Assert.assertEquals
import org.junit.Test

class WeekPatternParserTest {

    @Test
    fun `simple range`() {
        assertEquals(
            setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16),
            WeekPatternParser.parse("1-16周")
        )
    }

    @Test
    fun `range with extra single week`() {
        assertEquals(
            setOf(1, 2, 3, 4, 5, 6, 8),
            WeekPatternParser.parse("1-6,8周")
        )
    }

    @Test
    fun `odd weeks only`() {
        assertEquals(
            setOf(1, 3, 5, 7, 9, 11, 13, 15),
            WeekPatternParser.parse("1-16单周")
        )
    }

    @Test
    fun `even weeks only`() {
        assertEquals(
            setOf(2, 4, 6, 8, 10, 12, 14, 16),
            WeekPatternParser.parse("2-16双周")
        )
    }

    @Test
    fun `multiple ranges`() {
        assertEquals(
            setOf(1, 2, 3, 4, 5, 6, 8, 9, 10, 11, 12),
            WeekPatternParser.parse("1-6,8-12周")
        )
    }

    @Test
    fun `single weeks comma separated`() {
        assertEquals(
            setOf(7, 11),
            WeekPatternParser.parse("7,11周")
        )
    }

    @Test
    fun `with hours in parentheses`() {
        assertEquals(
            setOf(1, 2, 3, 4, 5, 6, 8),
            WeekPatternParser.parse("1-6,8周(32学时)")
        )
    }

    @Test
    fun `odd weeks with hours`() {
        assertEquals(
            setOf(1, 3, 5, 7, 9, 11, 13, 15),
            WeekPatternParser.parse("1-16单周(48学时)")
        )
    }

    @Test
    fun `single week with hours`() {
        assertEquals(
            setOf(3),
            WeekPatternParser.parse("3周(2学时)")
        )
    }

    @Test
    fun `empty string returns empty set`() {
        assertEquals(emptySet<Int>(), WeekPatternParser.parse(""))
    }

    @Test
    fun `blank string returns empty set`() {
        assertEquals(emptySet<Int>(), WeekPatternParser.parse("   "))
    }

    @Test
    fun `range 4-12 weeks`() {
        assertEquals(
            setOf(4, 5, 6, 7, 8, 9, 10, 11, 12),
            WeekPatternParser.parse("4-12周(64学时)")
        )
    }

    @Test
    fun `range 9-16 weeks`() {
        assertEquals(
            setOf(9, 10, 11, 12, 13, 14, 15, 16),
            WeekPatternParser.parse("9-16周(32学时)")
        )
    }

    @Test
    fun `range 4-11 weeks`() {
        assertEquals(
            setOf(4, 5, 6, 7, 8, 9, 10, 11),
            WeekPatternParser.parse("4-11周(32学时)")
        )
    }

    @Test
    fun `range 1-10 weeks`() {
        assertEquals(
            setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
            WeekPatternParser.parse("1-10周(32学时)")
        )
    }

    @Test
    fun `full width separators and parentheses`() {
        assertEquals(
            setOf(1, 2, 3, 4, 5, 6, 8),
            WeekPatternParser.parse("第1－6，8周（32学时）")
        )
    }

    @Test
    fun `parse or all falls back to whole semester for blank pattern`() {
        assertEquals(
            setOf(1, 2, 3, 4),
            WeekPatternParser.parseOrAll("", totalWeeks = 4)
        )
    }

    @Test
    fun `contains week falls back to whole semester for invalid pattern`() {
        assertEquals(
            true,
            WeekPatternParser.containsWeek("无法解析", week = 3, totalWeeks = 4)
        )
    }
}
