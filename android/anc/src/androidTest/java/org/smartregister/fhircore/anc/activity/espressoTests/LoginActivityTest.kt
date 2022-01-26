package org.smartregister.fhircore.anc.activity.espressoTests

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso
import org.junit.Rule
import org.smartregister.fhircore.engine.ui.login.LoginActivity
import java.lang.Thread.sleep

class LoginActivityTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule(LoginActivity::class.java)

    fun selectUsernameField() {
        composeTestRule.onNodeWithText("username").performClick()
    }
    fun selectPasswordField() {
        composeTestRule.onNodeWithText("password").performClick()
    }
    fun enterUsername(username: String) {
        composeTestRule.onNodeWithText("username").performTextInput(username)
    }
    fun enterPassword(password: String) {
        composeTestRule.onNodeWithText("password").performTextInput(password)
    }
    fun closeKeyboard() {
        Espresso.closeSoftKeyboard()
        sleep(100)
    }
    fun clickSignInButton() {
        composeTestRule.onNodeWithText("Login").performClick()
    }
    fun isSuccessfulLogin() {
    }
}