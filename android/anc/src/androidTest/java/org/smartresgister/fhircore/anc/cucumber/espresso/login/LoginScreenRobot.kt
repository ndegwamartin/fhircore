package org.smartregister.fhircore.anc.cucumber.espresso.login

//import android.support.test.espresso.Espresso
//import android.support.test.rule.ActivityTestRule
import androidx.compose.runtime.Composable
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
//import org.smartregister.fhircore.anc.R
import org.smartregister.fhircore.anc.ui.login.LoginActivity
import java.lang.Thread.sleep
import org.smartregister.fhircore.engine.R

class LoginScreenRobot {



    fun launchLoginScreen(testRule: ActivityTestRule<LoginActivity>) {
        testRule.launchActivity(null)
    }


    fun selectEmailField() {
        //onView(with())
        // onView(withId(R.id.o
//        InstrumentationRegistry.getInstrumentation().context
//                .getString(R.string.login_text)
//                .also { selectFrameByIndex(2);
//
//                }

                }


    fun selectPasswordField() {
       // onView(withId(R.id.password)).perform(click())
    }

    fun enterEmail(email: String) {
      //  onView(withId(R.id.email)).perform(typeText(email))
    }

    fun enterPassword(password: String) {
       // onView(withId(R.id.password)).perform(typeText(password))
    }

    fun closeKeyboard() {
        Espresso.closeSoftKeyboard()
        sleep(100)
    }

    fun clickSignInButton() {
       // onView(withText(InstrumentationRegistry.getTargetContext().getString(R.string.action_sign_in))).perform(click())
    }

    fun isSuccessfulLogin() {
       // onView(withId(R.id.successful_login_text_view)).check(matches(isDisplayed()))
      //  onView(withId(R.id.successful_login_text_view)).check(matches(withText(R.string.successful_login)))
    }

}