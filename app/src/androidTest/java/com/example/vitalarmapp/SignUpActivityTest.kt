package com.example.vitalarmapp

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vitalarmapp.models.User
import com.example.vitalarmapp.utils.firebase.RegistrationResult
import com.example.vitalarmapp.utils.firebase.UserRegistrationProvider
import com.example.vitalarmapp.utils.validation.RutValidationService
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignUpActivityTest {

    private lateinit var defaultRegistrationProvider: () -> UserRegistrationProvider
    private lateinit var defaultRutProvider: () -> RutValidationService

    @Before
    fun setup() {
        defaultRegistrationProvider = SignUpActivity.registrationProvider
        defaultRutProvider = SignUpActivity.rutValidationServiceProvider
    }

    @After
    fun tearDown() {
        SignUpActivity.registrationProvider = defaultRegistrationProvider
        SignUpActivity.rutValidationServiceProvider = defaultRutProvider
    }

    @Test
    fun registeringUserValidatesRutAndStoresInBackend() = runBlocking {
        val fakeBackend = InMemoryRegistrationProvider()
        val rutValidator = AlwaysValidRutService()

        SignUpActivity.registrationProvider = { fakeBackend }
        SignUpActivity.rutValidationServiceProvider = { rutValidator }

        val scenario = ActivityScenario.launch(SignUpActivity::class.java)

        onView(withId(R.id.signup_name_tf)).perform(replaceText("Test User"), closeSoftKeyboard())
        onView(withId(R.id.signup_rut_tf)).perform(replaceText("12.345.678-5"), closeSoftKeyboard())
        onView(withId(R.id.signup_email_tf)).perform(replaceText("test@example.com"), closeSoftKeyboard())
        onView(withId(R.id.signup_pass_tf)).perform(replaceText("strongPass"), closeSoftKeyboard())
        onView(withId(R.id.signup_confirm_pass_tf)).perform(replaceText("strongPass"), closeSoftKeyboard())

        onView(withId(R.id.signup_register_btn)).perform(click())

        onIdle()
        delay(100)

        assertEquals(1, fakeBackend.users.size)
        val storedUser = fakeBackend.users.first()
        assertEquals("Test User", storedUser.name)
        assertEquals("12.345.678-5", storedUser.rut)
        assertTrue(rutValidator.wasInvoked)

        scenario.close()
    }

    private class AlwaysValidRutService : RutValidationService {
        var wasInvoked = false
        override suspend fun isValid(rut: String): Boolean {
            wasInvoked = true
            return true
        }
    }

    private class InMemoryRegistrationProvider : UserRegistrationProvider {
        val users = mutableListOf<User>()

        override suspend fun registerUser(
            name: String,
            rut: String,
            email: String,
            password: String
        ): RegistrationResult {
            val user = User(id = "local-${users.size}", name = name, rut = rut, email = email, createdAt = System.currentTimeMillis())
            users.add(user)
            return RegistrationResult.Success(user)
        }

        override fun logout() { /* No-op for tests */ }
    }
}
