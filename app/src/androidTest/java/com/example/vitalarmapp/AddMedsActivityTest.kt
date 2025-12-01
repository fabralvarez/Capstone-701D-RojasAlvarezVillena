package com.example.vitalarmapp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddMedsActivityTest {

    @get:Rule
    val scenarioRule = ActivityScenarioRule(AddMedsActivity::class.java)

    @Test
    fun searchViewOpensAndShowsUp() {
        onView(withId(R.id.searchView)).check(matches(not(isDisplayed())))

        onView(withId(R.id.searchBar)).perform(click())

        onView(withId(R.id.searchView)).check(matches(isDisplayed()))
    }
}
