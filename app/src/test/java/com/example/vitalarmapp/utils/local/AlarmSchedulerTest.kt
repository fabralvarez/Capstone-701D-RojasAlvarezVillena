package com.example.vitalarmapp.utils.local

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class AlarmSchedulerTest {

    @Test
    fun `computeTriggerMillis moves to next day when time has passed`() {
        val now = LocalDateTime.of(LocalDate.of(2024, 12, 7), LocalTime.of(10, 0))
        val trigger = AlarmScheduler.computeTriggerMillis("09:00", now)
        val expected = LocalDateTime.of(LocalDate.of(2024, 12, 8), LocalTime.of(9, 0))
            .atZone(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        assertEquals(expected, trigger)
    }

    @Test
    fun `computeTriggerMillis keeps today when time is ahead`() {
        val now = LocalDateTime.of(LocalDate.of(2024, 12, 7), LocalTime.of(8, 0))
        val trigger = AlarmScheduler.computeTriggerMillis("09:00", now)
        val expected = LocalDateTime.of(LocalDate.of(2024, 12, 7), LocalTime.of(9, 0))
            .atZone(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        assertEquals(expected, trigger)
    }
}
