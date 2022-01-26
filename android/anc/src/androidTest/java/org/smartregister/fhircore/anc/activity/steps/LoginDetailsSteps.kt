package org.smartregister.fhircore.anc.activity.steps

import android.support.test.runner.AndroidJUnit4
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import cucumber.api.java.en.And
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When
import org.junit.Rule
import org.junit.runner.RunWith
import org.smartregister.fhircore.anc.activity.espressoTests.LoginActivityTest
import org.smartregister.fhircore.engine.ui.login.LoginActivity

@RunWith(AndroidJUnit4::class)
class LoginDetailsSteps {
    private val login = LoginActivityTest()
    @get:Rule
    val composeTestRule = createAndroidComposeRule(LoginActivity::class.java)

    @Given("^I start the application$")
    fun i_start_app() {
        login.composeTestRule
    }
    @When("^I click username field$")
    fun i_click_username_field() {
        login.selectUsernameField()
    }
    @And("^I close the keyboard$")
    fun i_close_the_keyboard() {
        login.closeKeyboard()
    }
    @And("^I enter valid username (\\S+)$")
    fun i_enter_valid_email(username: String) {
        login.enterUsername(username)
    }
    @And("^I click password field$")
    fun i_click_password_field() {
        login.selectPasswordField()
    }
    @And("^I enter valid password (\\S+)$")
    fun i_enter_valid_password(password: String) {
        login.enterPassword(password)
    }
    @And("^I click sign in button$")
    fun i_click_sign_in_button() {
        login.clickSignInButton()
    }
    @Then("^I expect to see successful login message$")
    fun i_expect_to_see_successful_login_message() {
        login.isSuccessfulLogin()
    }
}