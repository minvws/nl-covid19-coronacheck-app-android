package nl.rijksoverheid.ctr.holder.end2end.utils

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
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.findElement
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.tapButton
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.waitForText

object Actions {

    var chromeFirstVisit = true

    private fun addEvent() {
        tapButton("Menu")
        tapButton("Vaccinatie of test toevoegen")
    }

    fun addVaccinationCertificate(bsn: String) {
        addEvent()
        tapButton("Vaccinatie")
        retrieveCertificateFromServer(bsn)
    }

    fun addRecoveryCertificate(bsn: String) {
        addEvent()
        tapButton("Positieve test")
        retrieveCertificateFromServer(bsn)
    }

    fun addNegativeTestCertificateFromGGD(bsn: String) {
        addEvent()
        tapButton("Negatieve test")
        tapButton("GGD")
        retrieveCertificateFromServer(bsn)
    }

    private fun acceptChromeOnboarding() {
        if (checkForText("Welkom bij Chrome")) {
            waitForText("Accept")?.click()
        }
        if (checkForText("Synchronisatie aanzetten?", 2)) {
            waitForText("Nee, bedankt")?.click()
        }
        if (checkForText("Inloggen", 2)) {
            loginToServer()
        }
        if (checkForText("Chrome-meldingen maken het je makkelijker", 2)) {
            waitForText("Nee, bedankt")?.click()
        }
        if (checkForText("Toestaan dat Chrome je meldingen stuurt?", 2)) {
            waitForText("Niet toestaan")?.click()
        }
        chromeFirstVisit = false
    }

    private fun retrieveCertificateFromServer(bsn: String) {
        tapButton("Log in met DigiD")

        if (chromeFirstVisit) acceptChromeOnboarding()

        if (bsn.isEmpty()) TestCase.fail("BSN was null or empty, no certificate can be retrieved.")

        if (!checkForText("DigiD MOCK")) loginToServer()

        waitForText("999991772")?.text = bsn
        waitForText("Login / Submit")!!.click()
        waitForText("Kloppen de gegevens?", 15)
    }

    private fun loginToServer() {
        if (BaseTest.authPassword.isNullOrEmpty()) {
            TestCase.fail("Password was null or empty, no certificate can be retrieved.")
        }

        val password = findElement(android.widget.EditText::class.java, "Wachtwoord")!!
        password.click()
        password.text = BaseTest.authPassword

        val username = findElement(android.widget.EditText::class.java, "Gebruikersnaam")!!
        username.click()
        username.text = "coronacheck"

        val submit = findElement(android.widget.Button::class.java, "Inloggen")!!
        submit.click()
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
