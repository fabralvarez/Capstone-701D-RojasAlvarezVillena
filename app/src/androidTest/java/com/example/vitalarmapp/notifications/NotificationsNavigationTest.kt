package com.example.vitalarmapp.notifications

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vitalarmapp.AddMainTabActivity
import com.example.vitalarmapp.LanMenuActivity
import com.example.vitalarmapp.NotificationsActivity
import com.example.vitalarmapp.ProfileTabActivity
import com.example.vitalarmapp.R
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationsNavigationTest {

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun lanMenuToolbarBellOpensNotifications() {
        ActivityScenario.launch(LanMenuActivity::class.java)

        onView(withContentDescription(R.string.notifications_action_label))
            .perform(click())

        Intents.intended(hasComponent(NotificationsActivity::class.java.name))
    }

    @Test
    fun addMainTabToolbarBellOpensNotifications() {
        ActivityScenario.launch(AddMainTabActivity::class.java)

        onView(withContentDescription(R.string.notifications_action_label))
            .perform(click())

        Intents.intended(hasComponent(NotificationsActivity::class.java.name))
    }

    @Test
    fun profileToolbarBellOpensNotifications() {
        ActivityScenario.launch(ProfileTabActivity::class.java)

        onView(withContentDescription(R.string.notifications_action_label))
            .perform(click())

        Intents.intended(hasComponent(NotificationsActivity::class.java.name))
    }
}
