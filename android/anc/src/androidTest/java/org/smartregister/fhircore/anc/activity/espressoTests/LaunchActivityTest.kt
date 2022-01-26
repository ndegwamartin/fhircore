package org.smartregister.fhircore.anc.activity.espressoTests

import android.support.test.runner.AndroidJUnit4
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.smartregister.fhircore.engine.ui.login.LoginActivity

@RunWith(AndroidJUnit4::class)
class LaunchActivityTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule(LoginActivity::class.java)
    
    @Test
    public fun enterApplicationID() {
        composeTestRule.onNodeWithText("Enter Application ID").performTextInput("")
        composeTestRule.onNodeWithText("Remember application").performClick()
        composeTestRule.onNodeWithText("LOAD CONFIGURATIONS").performClick()
    }
}