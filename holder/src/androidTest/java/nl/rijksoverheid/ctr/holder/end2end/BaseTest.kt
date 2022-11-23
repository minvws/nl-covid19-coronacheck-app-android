package nl.rijksoverheid.ctr.holder.end2end

import android.app.Instrumentation
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.adevinta.android.barista.interaction.BaristaClickInteractions.clickOn
import com.adevinta.android.barista.interaction.BaristaDialogInteractions.clickDialogPositiveButton
import nl.rijksoverheid.ctr.holder.HolderMainActivity
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.checkForText
import org.junit.Before
import org.junit.Rule

abstract class BaseTest {

    @get:Rule
    var rule = activityScenarioRule<HolderMainActivity>()

    @Before
    fun skipOnboarding() {
        if (checkForText("Security risks have been found")) {
            clickDialogPositiveButton()
        }
        if (checkForText("Travel safely with your certificate")) {
            clickOn("Next")

            if (checkForText("Certificate of vaccination, recovery or test")) {
                clickOn("Next")
            }
            if (checkForText("Your certificate contains a QR code")) {
                clickOn("Next")
            }
            if (checkForText("This is how the app uses your data")) {
                clickOn("Get started")
            }
        }
    }

    companion object {
        private val instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation()
        val device: UiDevice = UiDevice.getInstance(instrumentation)
    }
}
