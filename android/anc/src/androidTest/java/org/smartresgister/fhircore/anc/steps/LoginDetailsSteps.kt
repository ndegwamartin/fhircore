package org.smartregister.fhircore.anc.steps


import android.app.Application
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider

import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.ui.test.assertIsDisplayed
import androidx.ui.test.findByText
import org.smartregister.fhircore.anc.ui.login.LoginActivity
import cucumber.api.java.After
import cucumber.api.java.Before
import cucumber.api.java.en.And
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.smartregister.fhircore.engine.R
//import org.smartregister.fhircore.anc.R
import org.smartregister.fhircore.anc.cucumber.espresso.login.LoginScreenRobot
import org.smartregister.fhircore.engine.auth.AuthenticationService
import org.smartregister.fhircore.engine.configuration.app.ConfigurableApplication
import org.smartregister.fhircore.engine.configuration.view.loginViewConfigurationOf
import org.smartregister.fhircore.engine.ui.login.BaseLoginActivity
import org.smartregister.fhircore.engine.ui.login.LoginPage
import org.smartregister.fhircore.engine.ui.login.LoginScreen
import org.smartregister.fhircore.engine.ui.login.LoginViewModel
import org.smartregister.fhircore.engine.ui.questionnaire.QuestionnaireActivity
import org.smartregister.fhircore.engine.ui.questionnaire.QuestionnaireViewModel
import org.smartregister.fhircore.engine.ui.theme.AppTheme
import org.smartregister.fhircore.engine.util.extension.assertIsConfigurable
import org.smartregister.fhircore.engine.util.extension.createFactory
import org.smartregister.fhircore.engine.util.extension.runSync


@RunWith(AndroidJUnit4::class)
class LoginDetailsStep {

    private val robot = LoginScreenRobot()
    private lateinit var LoginViewModel: LoginViewModel


    @get:Rule
   val composeTestRule = createAndroidComposeRule<BaseLoginActivity>()


//    @get:Rule
//    val composeTestRule = createComposeRule()

//    @get:Rule
//    var activityScenarioRule: ActivityScenarioRule<LoginActivity> = ActivityScenarioRule(LoginActivity::class.java)
//

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
//        composeTestRule.setContent {
//            AppTheme {
//               LoginScreen(loginViewModel = LoginViewModel(ApplicationProvider.getApplicationContext(),(ApplicationProvider.getApplicationContext<Application>() as ConfigurableApplication).authenticationService, loginViewConfigurationOf()))
//            }
//        }

    }

    @After
    fun tearDown() {
        Intents.release() // Required for test scenarios with multiple activities or scenarios with more cases
       // composeTestRule.activity.finish()
    }


    @Given("^I start the application$")
    fun i_start_app() {
        Intents.init()
        ActivityScenario.launch(LoginActivity::class.java)
        Thread.sleep(5000)

        }



    @When("^I click email field$")
    @Test
    fun i_click_email_field() {

//        println("**************************************************************************************************")
       composeTestRule.onNodeWithText("Enter username").assertExists()
//        println("**************************************************************************************************))")
//        InstrumentationRegistry.getInstrumentation().context
//                .getString(R.string.username_input_hint)

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