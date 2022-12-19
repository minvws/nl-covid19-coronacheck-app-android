package nl.rijksoverheid.ctr.holder.end2end.utils

import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.adevinta.android.barista.interaction.BaristaClickInteractions.clickBack
import com.adevinta.android.barista.interaction.BaristaClickInteractions.clickOn
import junit.framework.TestCase.fail
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.end2end.BaseTest
import nl.rijksoverheid.ctr.holder.end2end.model.Event
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertOverview
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.card
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.checkForText
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.findElement
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.tapButton
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.waitForText

object Actions {

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
        scrollTo("Negatieve test")
        tapButton("Negatieve test")
        tapButton("GGD")
        retrieveCertificateFromServer(bsn)
    }

    fun addRetrievedCertificateToApp() {
        tapButton("Maak bewijs")
        waitForText("Mijn bewijzen", 60)
    }

    fun viewQR(eventType: Event.Type) {
        card(eventType).tapButton("Bekijk QR")
        assertDisplayed("Internationale QR")
    }

    fun viewPreviousQR() {
        clickOn(R.id.previousQrButton)
    }

    fun backToOverview() {
        clickBack()
        assertOverview()
    }

    private fun retrieveCertificateFromServer(bsn: String) {
        if (bsn.isEmpty()) fail("BSN was null or empty, no certificate can be retrieved.")
        if (BaseTest.authPassword.isNullOrEmpty()) fail("Password was null or empty, no certificate can be retrieved.")

        tapButton("Log in met DigiD")

        if (!checkForText("DigiD MOCK", 5)) loginToServer()
        waitForText("999991772")?.text = bsn
        waitForText("Login / Submit")!!.click()
        waitForText("Kloppen de gegevens?", 30)
    }

    private fun loginToServer() {
        while (!checkForText("Inloggen", 1)) acceptChromeOnboarding()

        val password = findElement(android.widget.EditText::class.java, "Wachtwoord")!!
        password.click()
        password.text = BaseTest.authPassword

        val username = findElement(android.widget.EditText::class.java, "Gebruikersnaam")!!
        username.click()
        username.text = "coronacheck"

        val submit = findElement(android.widget.Button::class.java, "Inloggen")!!
        submit.click()
    }

    private fun acceptChromeOnboarding() {
        if (checkForText("Welkom bij Chrome", 1)) {
            waitForText("Accept")?.click()
        }
        if (checkForText("Synchronisatie aanzetten?", 1)) {
            waitForText("Nee, bedankt")?.click()
        }
        if (checkForText("Chrome-meldingen maken het je makkelijker", 1)) {
            waitForText("Nee, bedankt")?.click()
        }
        if (checkForText("Toestaan dat Chrome je meldingen stuurt?", 1)) {
            waitForText("Niet toestaan")?.click()
        }
    }
}
