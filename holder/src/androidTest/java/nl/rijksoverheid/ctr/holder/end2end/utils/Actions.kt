package nl.rijksoverheid.ctr.holder.end2end.utils

import android.os.Build
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.adevinta.android.barista.interaction.BaristaClickInteractions.clickBack
import com.adevinta.android.barista.interaction.BaristaClickInteractions.clickOn
import com.adevinta.android.barista.interaction.BaristaScrollInteractions.scrollTo
import junit.framework.TestCase.fail
import nl.rijksoverheid.ctr.holder.BuildConfig
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.end2end.BaseTest
import nl.rijksoverheid.ctr.holder.end2end.model.Event
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertOverview
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.card
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.checkForText
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.checkTextExists
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.enterBsn
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.enterTextInField
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.rest
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.tapButton
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.tapButtonElement
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.waitForText
import timber.log.Timber

object Actions {

    fun logVersions() {
        val appVersion = BuildConfig.VERSION_NAME
        val appVersionCode = BuildConfig.VERSION_CODE
        val release = Build.VERSION.RELEASE
        val sdkVersion = Build.VERSION.SDK_INT
        val logLine = "App $appVersion ($appVersionCode), Android $release (SDK $sdkVersion)"

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            Timber.tag("end2end").d(logLine)
        } else {
            println("end2end: $logLine")
        }
    }

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
        if (bsn.isEmpty()) fail("BSN was empty, no certificate can be retrieved.")
        if (BaseTest.authPassword.isEmpty()) fail("Password was empty, no certificate can be retrieved.")

        scrollTo("Log in met DigiD")
        tapButton("Log in met DigiD")

        for (index in 1 until 4) {
            Timber.tag("end2end").d("Log in attempt $index")
            if (!checkTextExists("DigiD Mock")) loginToServer() else break
        }

        enterBsn(bsn)

        if (checkForText("Openen met", 3)) {
            waitForText("CoronaCheck (Acc)")!!.click()
            tapButtonElement("Altijd")
        }
        waitForText("Kloppen de gegevens?", 30)
    }

    private fun loginToServer() {
        if (checkForText("Help bij het maken van een beter toetsenbord")) tapButtonElement("Sluiten")
        if (checkForText("Inloggen") || checkForText("Verificatie vereist")) {
            enterTextInField(0, "coronacheck")
            enterTextInField(1, BaseTest.authPassword)
            tapButtonElement("Inloggen")
            rest()
        } else {
            acceptChromeOnboarding()
        }
    }

    private fun acceptChromeOnboarding() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (checkForText("Welkom bij Chrome")) tapButtonElement("Accept")
            if (checkForText("Synchronisatie aanzetten?")) tapButtonElement("Nee, bedankt")
            if (checkForText("Inloggen bij Chrome")) tapButtonElement("Nee, bedankt")
            if (checkForText("Toestaan dat Chrome je meldingen stuurt?")) tapButtonElement("Niet toestaan")
            if (checkForText("meldingen")) tapButtonElement("Nee, bedankt")
            if (checkForText("Wil je dat Google Chrome je wachtwoord voor deze site opslaat?")) tapButtonElement("Nooit")
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                if (checkForText("Wachtwoord opslaan?")) tapButtonElement("Nooit")
            }
        }
    }
}
