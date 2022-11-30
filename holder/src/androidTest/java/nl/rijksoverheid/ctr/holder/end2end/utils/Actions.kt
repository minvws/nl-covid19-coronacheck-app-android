package nl.rijksoverheid.ctr.holder.end2end.utils

import androidx.test.uiautomator.UiSelector
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.adevinta.android.barista.interaction.BaristaClickInteractions.clickBack
import com.adevinta.android.barista.interaction.BaristaClickInteractions.clickOn
import junit.framework.TestCase
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.end2end.BaseTest
import nl.rijksoverheid.ctr.holder.end2end.model.Event
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertOverview
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertQRisHidden
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.card
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.checkForText
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.tapButton
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.waitForText

object Actions {

    private fun addEvent() {
        tapButton("Menu")
        tapButton("Vaccinatie of test toevoegen")
    }

    fun addVaccinationCertificate(bsn: String) {
        addEvent()
        tapButton("Ik heb een (booster)vaccinatie gehad")
        tapButton("Log in met DigiD")
        retrieveCertificateFromServer(bsn)
    }

    private fun retrieveCertificateFromServer(bsn: String) {
        if (bsn.isEmpty()) {
            TestCase.fail("BSN was null or empty, no certificate can be retrieved.")
        }

        val loggedIn = waitForText("DigiD MOCK")
        if (loggedIn == null) loginToServer()

        val inputBsn = waitForText("999991772")
        if (inputBsn != null) {
            inputBsn.text = bsn
            waitForText("Login / Submit")!!.click()
            waitForText("Kloppen de gegevens?", 15)
        }
    }

    private fun loginToServer() {
        if (BaseTest.authPassword.isNullOrEmpty()) {
            TestCase.fail("Password was null or empty, no certificate can be retrieved.")
        }

        val password = waitForText("Wachtwoord")
        password!!.click()
        password.text = BaseTest.authPassword

        val username = waitForText("Gebruikersnaam")
        username!!.click()
        username.text = "coronacheck"

        val submitButton = BaseTest.device.findObject(
            UiSelector()
                .className(android.widget.Button::class.java.canonicalName!!)
                .textStartsWith("Inloggen")
        )
        submitButton.click()
    }

    fun addRetrievedCertificateToApp() {
        checkForText("Kloppen de gegevens?")
        tapButton("Maak bewijs")
        assertOverview()
    }

    fun viewQR(eventType: Event.Type) {
        card(eventType).tapButton("Bekijk QR")
        assertDisplayed("Internationale QR")
    }

    fun viewPreviousQR(hidden: Boolean = false) {
        clickOn(R.id.previousQrButton)
        if (hidden) assertQRisHidden()
    }

    fun backToOverview() {
        clickBack()
        assertOverview()
    }
}
