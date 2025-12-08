package com.example.vitalarmapp.notifications

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vitalarmapp.NotificationsActivity
import com.example.vitalarmapp.R
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.not
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationsActivityRenderTest {

    @Test
    fun showsRecyclerWithData() {
        val scenario = ActivityScenario.launch(NotificationsActivity::class.java)

        onView(withId(R.id.notificationsRecycler)).check(matches(isDisplayed()))
        onView(withId(R.id.notificationsEmpty)).check(matches(not(isDisplayed())))

        scenario.onActivity { activity ->
            val itemCount = activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.notificationsRecycler)
                .adapter?.itemCount ?: 0
            assertThat(itemCount, greaterThan(0))
        }
    }
}