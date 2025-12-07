package com.example.vitalarmapp.utils.local

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlarmRepositoryInstrumentedTest {

    private lateinit var repository: AlarmRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        context.getSharedPreferences("alarms_repository", android.content.Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        repository = AlarmRepository(context)
    }

    @Test
    fun savesAndFiltersHistory() {
        val recent = AlarmRecord(
            patientName = "Paciente Uno",
            medicationName = "Med A",
            medicationDetail = "200mg",
            time = "09:00",
            scheduledAt = System.currentTimeMillis(),
            triggeredAt = System.currentTimeMillis(),
            photoPath = "/tmp/photoA.jpg"
        )
        val old = recent.copy(
            id = "old",
            triggeredAt = System.currentTimeMillis() - (9 * 86_400_000L),
            photoPath = "/tmp/photoB.jpg"
        )

        repository.save(recent)
        repository.save(old)

        val history = repository.getHistory()
        assertEquals(1, history.size)
        assertEquals(recent.id, history.first().id)
    }
}
