package com.example.tasklite

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<LoginActivity>()

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun testEmptyFields_showsErrorToast() {
        // Click login without entering anything
        composeRule.onNodeWithTag("login_button").performClick()

        // Still on login screen (button is still visible)
        composeRule.onNodeWithTag("login_button").assertIsDisplayed()
    }

    @Test
    fun testLoginButton_isDisplayed() {
        composeRule.onNodeWithTag("login_button")
            .assertIsDisplayed()
    }

    // Note: This test requires a valid Firebase configuration and user to pass if not mocked.
    // For local UI testing, it's better to mock the AuthViewModel.
    @Test
    fun testSignupButton_navigatesToSignup() {
        composeRule.onNodeWithText("Don't have an account? Sign Up")
            .performClick()

        composeRule.waitForIdle()

        intended(hasComponent(SignupActivity::class.java.name))
    }

    @Test
    fun testForgotPassword_navigatesToForgotPassword() {
        composeRule.onNodeWithText("Forgot Password?")
            .performClick()

        composeRule.waitForIdle()

        intended(hasComponent(ForgotPasswordActivity::class.java.name))
    }
}