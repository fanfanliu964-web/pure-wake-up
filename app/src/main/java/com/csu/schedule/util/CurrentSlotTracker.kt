package com.csu.schedule.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay

data class CurrentSlot(val dayOfWeek: Int, val section: Int?)

@Composable
fun rememberCurrentSlot(): State<CurrentSlot> = produceState(
    initialValue = CurrentSlot(
        dayOfWeek = WeekCalculator.todayDayOfWeek(),
        section = TimeSlots.currentSection()
    )
) {
    while (true) {
        delay(60_000L)
        value = CurrentSlot(
            dayOfWeek = WeekCalculator.todayDayOfWeek(),
            section = TimeSlots.currentSection()
        )
    }
}
