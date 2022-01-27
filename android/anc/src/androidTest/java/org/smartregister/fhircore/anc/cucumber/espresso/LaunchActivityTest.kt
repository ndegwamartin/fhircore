package org.smartregister.fhircore.anc.cucumber.espresso

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test
import org.smartregister.fhircore.engine.ui.login.LoginActivity

class LaunchActivityTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule(LoginActivity::class.java)
    
    @Test
    public fun enterApplicationID() {
        composeTestRule.onNodeWithText("Username").performTextInput("demo")
        composeTestRule.onNodeWithText("Password").performTextInput("Amani123")
        composeTestRule.onNode(hasText("")).assertExists();
    }
}