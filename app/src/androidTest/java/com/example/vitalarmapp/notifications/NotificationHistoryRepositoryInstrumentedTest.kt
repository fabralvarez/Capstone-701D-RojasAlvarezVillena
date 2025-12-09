package com.example.vitalarmapp.notifications

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationHistoryRepositoryInstrumentedTest {

    private val repository = NotificationHistoryRepository()
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        repository.clear(context)
    }

    @After
    fun tearDown() {
        repository.clear(context)
    }

    @Test
    fun savesEntriesInReverseChronologicalOrder() {
        val first = NotificationEntry("Primera", "detalle", 1_000L)
        val second = NotificationEntry("Segunda", "detalle", 2_000L)

        repository.addEntry(context, first)
        repository.addEntry(context, second)

        val entries = repository.loadEntries(context)

        assertEquals(2, entries.size)
        assertEquals("Segunda", entries.first().title)
    }

    @Test
    fun trimsEntriesToMaximumLimit() {
        repeat(12) { index ->
            repository.addEntry(
                context,
                NotificationEntry(
                    title = "Entry $index",
                    detail = "detail",
                    timestamp = 1_000L + index
                )
            )
        }

        val entries = repository.loadEntries(context)

        assertEquals(10, entries.size)
        assertTrue(entries.first().title.contains("Entry 11"))
    }
}
