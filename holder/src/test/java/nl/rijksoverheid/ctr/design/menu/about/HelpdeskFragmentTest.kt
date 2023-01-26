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
import androidx.test.platform.app.InstrumentationRegistry
import nl.rijksoverheid.ctr.holder.R
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
        val context = InstrumentationRegistry.getInstrumentation().context
        launchFragmentInContainer<HelpdeskFragment>(
            bundleOf(
                "data" to HelpdeskData(
                    contactTitle = "Contact",
                    contactMessage = context.getString(
                        R.string.holder_helpdesk_contact_message,
                        "0800-1421",
                        "0800-1421",
                        "+31707503720",
                        "+31707503720",
                        1,
                        "08:00",
                        5,
                        "18:00"
                    ),
                    supportTitle = "Ondersteuning",
                    supportMessage = "Wanneer je contact opneemt met de CoronaCheck helpdesk, kan er gevraagd worden om de volgende informatie:",
                    appVersionTitle = "App-versie:",
                    appVersion = "version",
                    configurationTitle = "Configuratie:",
                    configuration = "configuration"
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

        onView(withText(containsString("+31707503720")))
            .perform(openLinkWithText("+31707503720"))

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
