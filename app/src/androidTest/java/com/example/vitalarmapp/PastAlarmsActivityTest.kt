package com.example.vitalarmapp

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vitalarmapp.utils.local.AlarmRecord
import com.example.vitalarmapp.utils.local.AlarmRepository
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PastAlarmsActivityTest {

    @Before
    fun seedData() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val repository = AlarmRepository(context)
        context.getSharedPreferences("alarms_repository", android.content.Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        repository.save(
            AlarmRecord(
                patientName = "Paciente QA",
                medicationName = "Med QA",
                medicationDetail = "100mg",
                time = "10:30",
                scheduledAt = System.currentTimeMillis(),
                triggeredAt = System.currentTimeMillis(),
                photoPath = "/tmp/qa.jpg"
            )
        )
    }

    @Test
    fun showsPastAlarmCard() {
        ActivityScenario.launch(PastAlarmsActivity::class.java).use {
            onView(withId(R.id.past_alarms_list)).check(matches(isDisplayed()))
        }
    }
}
