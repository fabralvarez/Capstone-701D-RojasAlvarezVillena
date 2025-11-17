package com.example.vitalarmapp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    @get:Rule
    val intentsRule = IntentsRule()

    @get:Rule
    val scenarioRule = ActivityScenarioRule(LoginActivity::class.java)

    @Test
    fun clickingNavigationIcon_returnsToMainActivity() {
        onView(withContentDescription(R.string.login_toolbar_navigation_description))
            .perform(click())

        intended(hasComponent(MainActivity::class.java.name))
    }
}
