package org.smartregister.fhircore.anc.steps

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.cucumber.core.api.Scenario
import io.cucumber.java.After
import io.cucumber.java.Before
import io.cucumber.java.en.When
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.And
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.smartregister.fhircore.anc.AncApplication
import org.smartregister.fhircore.anc.MainActivity
import org.smartregister.fhircore.anc.R

import org.smartregister.fhircore.anc.cucumber.espresso.login.LoginScreenRobot
import org.smartregister.fhircore.anc.ui.login.LoginActivity
import org.smartregister.fhircore.engine.configuration.app.ConfigurableApplication
import org.smartregister.fhircore.engine.configuration.view.loginViewConfigurationOf
import org.smartregister.fhircore.engine.ui.login.LoginScreen
import org.smartregister.fhircore.engine.ui.login.LoginViewModel
import org.smartregister.fhircore.engine.ui.theme.AppTheme


@RunWith(AndroidJUnit4::class)
class LoginDetailsSteps{

    private val robot = LoginScreenRobot()

    @Rule
    var activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setup(scenario: Scenario) {
    }
    @After
    fun tearDown() {
      //  ActivityFinisher.finishOpenActivities() // Required for test scenarios with multiple activities or scenarios with more cases
    }


    @Given("^I start the application$")
    fun i_start_app() {
        Intents.init()
        ActivityScenario.launch(MainActivity::class.java)

    }


    @When("^I click email field$")
    fun i_click_email_field() {

    }

    @And("^I close the keyboard$")
    fun i_close_the_keyboard() {
        robot.closeKeyboard()
    }

    @And("^I enter valid email (\\S+)$")
    fun i_enter_valid_email(email: String) {
        onView(withId(R.id.editText)).perform(typeText(email))
    }

    @And("^I click password field$")
    fun i_click_password_field() {
        robot.selectPasswordField()
    }

    @And("^I enter valid password (\\S+)$")
    fun i_enter_valid_password(password: String) {
        robot.enterPassword(password)
    }

    @And("^I click sign in button$")
    fun i_click_sign_in_button() {
        onView(withId(R.id.button)).perform(click())
    }

    @Then("^I expect to see successful login message$")
    fun i_expect_to_see_successful_login_message() {
        robot.isSuccessfulLogin()
    }

}