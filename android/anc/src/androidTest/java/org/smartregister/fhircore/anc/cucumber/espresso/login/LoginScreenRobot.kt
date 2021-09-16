package org.smartregister.fhircore.anc.cucumber.espresso.login


import androidx.test.espresso.Espresso
import androidx.test.rule.ActivityTestRule
import org.smartregister.fhircore.anc.ui.login.LoginActivity
import java.lang.Thread.sleep

class LoginScreenRobot {

    fun launchLoginScreen(testRule: ActivityTestRule<LoginActivity>) {
        testRule.launchActivity(null)
    }

    fun selectEmailField() {
      //  onView(withId(R.id.email)).perform(click())
    }

    fun selectPasswordField() {
      //  onView(withId(R.id.password)).perform(click())
    }

    fun enterEmail(email: String) {
      //  onView(withId(R.id.email)).perform(typeText(email))
    }

    fun enterPassword(password: String) {
        //onView(withId(R.id.password)).perform(typeText(password))
    }

    fun closeKeyboard() {
        Espresso.closeSoftKeyboard()
        sleep(100)
    }

    fun clickSignInButton() {
    //    onView(withText(InstrumentationRegistry.getTargetContext().getString(R.string.action_sign_in))).perform(click())
    }

    fun isSuccessfulLogin() {
//        onView(withId(R.id.successful_login_text_view)).check(matches(isDisplayed()))
//        onView(withId(R.id.successful_login_text_view)).check(matches(withText(R.string.successful_login)))
    }

}