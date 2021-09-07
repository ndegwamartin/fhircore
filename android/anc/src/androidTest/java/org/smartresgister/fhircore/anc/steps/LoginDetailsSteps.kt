package org.smartregister.fhircore.anc.steps


import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider

import androidx.test.espresso.intent.Intents

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.smartregister.fhircore.anc.ui.login.LoginActivity
import cucumber.api.java.After
import cucumber.api.java.Before
import cucumber.api.java.en.And
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When
import org.junit.Rule
import org.junit.runner.RunWith
import org.smartregister.fhircore.anc.cucumber.espresso.login.LoginScreenRobot



@RunWith(AndroidJUnit4::class)
class LoginDetailsSteps{

    private val robot = LoginScreenRobot()

    @Rule
    val composeTestRule = createComposeRule()


//   @Rule
//        val composeTestRule = createAndroidComposeRule(LoginActivity::class.java)

//    @Rule
//    var activityScenarioRule = ActivityScenarioRule(LoginActivity::class.java)

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent{ApplicationProvider.getApplicationContext<LoginActivity>() }
     //   composeTestRule.setContent { LoginScreenPreview() }

    }

    @After
    fun tearDown() {
        Intents.release() // Required for test scenarios with multiple activities or scenarios with more cases
    }

    @Given("^I start the application$")
    fun i_start_app() {
        Intents.init()
        ActivityScenario.launch(LoginActivity::class.java)


    }

    @When("^I click email field$")
    fun i_click_email_field() {
        println(composeTestRule.onRoot().printToString())
//        Thread.sleep(5000)
//        composeTestRule.onNodeWithText("").performTextInput("demo")

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