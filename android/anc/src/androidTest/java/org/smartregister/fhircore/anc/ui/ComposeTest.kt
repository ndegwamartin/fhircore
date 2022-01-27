package org.smartregister.fhircore.anc.activity.espressoTests

import androidx.compose.material.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.smartregister.fhircore.engine.ui.theme.AppTheme

class ComposeTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    // use createAndroidComposeRu
    // le<YourActivity>() if you need access to
    // an activity

    @Test
    fun MyTest() {
        // Start the app
        composeTestRule.setContent { AppTheme { Text(text = "Welcome") } }

        composeTestRule.onNodeWithText("Welcome").assertIsDisplayed()
    }
}