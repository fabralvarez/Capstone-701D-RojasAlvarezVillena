package com.example.vitalarmapp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.google.android.material.R as MaterialR
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddMedsActivityTest {

    @get:Rule
    val scenarioRule = ActivityScenarioRule(AddMedsActivity::class.java)

    @Test
    fun searchViewOpensAndAcceptsInput() {
        onView(withId(R.id.searchView)).check(matches(not(isDisplayed())))

        onView(withId(R.id.searchBar)).perform(click())

        onView(withId(R.id.searchView)).check(matches(isDisplayed()))

        val queryText = "aspirin"
        onView(withId(MaterialR.id.search_view_edit_text))
            .perform(typeText(queryText), closeSoftKeyboard())
            .check(matches(withText(queryText)))
    }
}
