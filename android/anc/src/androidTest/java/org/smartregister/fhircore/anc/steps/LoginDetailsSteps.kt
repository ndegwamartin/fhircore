package org.smartregister.fhircore.anc.steps

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
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
    var activityScenarioRule = ActivityScenarioRule(LoginActivity::class.java)


    @Rule
    val composeTestRule = createAndroidComposeRule<LoginActivity>()


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
        ActivityScenario.launch(LoginActivity::class.java)

    }


    @When("^I click email field$")
    fun i_click_email_field() {
        val loginViewModel = LoginViewModel(
                application = AncApplication.getContext(),
                authenticationService = (AncApplication.getContext() as ConfigurableApplication).authenticationService,
                loginViewConfiguration = loginViewConfigurationOf("ANC", "0.0.1", true)
        )

        composeTestRule.runOnUiThread {
            loginViewModel.onUsernameUpdated("demo")
            loginViewModel.onPasswordUpdated("Amani123")
        }

        composeTestRule.setContent {
            AppTheme {
                LoginScreen(loginViewModel = loginViewModel)
            }
        }
        composeTestRule.onNodeWithText("Subhan").assertIsDisplayed()
        composeTestRule.onNodeWithText("demo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Amani123").assertIsDisplayed()
        composeTestRule.onNodeWithText("LOGIN").performClick()
    }

    @And("^I close the keyboard$")
    fun i_close_the_keyboard() {
        robot.closeKeyboard()
    }

    @And("^I enter valid email (\\S+)$")
    fun i_enter_valid_email(email: String) {
        robot.enterEmail(email)
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
        robot.clickSignInButton()
    }

    @Then("^I expect to see successful login message$")
    fun i_expect_to_see_successful_login_message() {
        robot.isSuccessfulLogin()
    }

}