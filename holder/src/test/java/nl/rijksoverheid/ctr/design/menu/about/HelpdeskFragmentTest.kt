package nl.rijksoverheid.ctr.design.menu.about

import android.app.Instrumentation
import android.content.Intent
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.openLinkWithText
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HelpdeskFragmentTest : AutoCloseKoinTest() {

    @Before
    fun setup() {
        launchFragmentInContainer<HelpdeskFragment>(
            bundleOf(
                "data" to HelpdeskData(
                    "version",
                    "configuration"
                )
            )
        )
        Intents.init()
    }

    @Test
    fun `clicking on 0800 phone number opens phone dialer`() {
        val expectedIntent = allOf(hasAction(Intent.ACTION_VIEW), hasData("tel:0800-1421"))
        intending(expectedIntent).respondWith(Instrumentation.ActivityResult(0, null))

        onView(withText(containsString("0800-1421")))
            .perform(openLinkWithText("0800-1421"))

        Intents.intended(expectedIntent)
    }

    @Test
    fun `clicking on landline phone number opens phone dialer`() {
        val expectedIntent = allOf(hasAction(Intent.ACTION_VIEW), hasData("tel:+31707503720"))
        intending(expectedIntent).respondWith(Instrumentation.ActivityResult(0, null))

        onView(withText(containsString("+31 70 750 37 20")))
            .perform(openLinkWithText("+31 70 750 37 20"))

        Intents.intended(expectedIntent)
    }

    @Test
    fun `clicking on email link opens email client`() {
        val expectedIntent =
            allOf(hasAction(Intent.ACTION_VIEW), hasData("mailto:helpdesk@coronacheck.nl"))
        intending(expectedIntent).respondWith(Instrumentation.ActivityResult(0, null))

        onView(withText(containsString("helpdesk@coronacheck.nl")))
            .perform(openLinkWithText("helpdesk@coronacheck.nl"))

        Intents.intended(expectedIntent)
    }

    @After
    fun tearDown() {
        Intents.release()
    }
}
