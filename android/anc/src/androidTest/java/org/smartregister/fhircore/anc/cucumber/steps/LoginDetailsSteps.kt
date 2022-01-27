package org.smartregister.fhircore.anc.cucumber.steps

import android.support.test.runner.AndroidJUnit4
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.cucumber.java.bs.I
import io.cucumber.java.en.Given
import org.junit.runner.RunWith
import org.smartregister.fhircore.anc.cucumber.test.ActivityScenarioHolder
import org.smartregister.fhircore.anc.cucumber.test.ComposeRuleHolder

@RunWith(AndroidJUnit4::class)
class LoginDetailsSteps (val composeRuleHolder: ComposeRuleHolder, val scenarioHolder: ActivityScenarioHolder):
    SemanticsNodeInteractionsProvider by composeRuleHolder.composeRule {

        @Given(I start the application)
        fun initializeActivity(){

        }


}