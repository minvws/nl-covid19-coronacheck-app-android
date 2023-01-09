package nl.rijksoverheid.ctr.holder.end2end.utils

import android.content.Intent
import android.os.Build
import android.provider.Settings
import junit.framework.TestCase.fail
import nl.rijksoverheid.ctr.holder.BuildConfig
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.end2end.BaseTest.Companion.authPassword
import nl.rijksoverheid.ctr.holder.end2end.BaseTest.Companion.context
import nl.rijksoverheid.ctr.holder.end2end.BaseTest.Companion.device
import nl.rijksoverheid.ctr.holder.end2end.model.Event
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.assertDisplayed
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.card
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.checkForText
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.clickOn
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.enterBsn
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.enterTextInField
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.scrollListToPosition
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.scrollTo
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.tapButton
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.tapButtonElement
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.tapOnElementWithContentDescription
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.waitForText
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.waitForView
import timber.log.Timber

object Actions {

    fun logVersions() {
        val appVersion = BuildConfig.VERSION_NAME
        val appVersionCode = BuildConfig.VERSION_CODE
        val release = Build.VERSION.RELEASE
        val sdkVersion = Build.VERSION.SDK_INT
        val logLine = "App $appVersion ($appVersionCode), Android $release (SDK $sdkVersion)"

        Timber.tag("end2end").d(logLine)
    }

    // Based on https://stackoverflow.com/a/58359193
    @Suppress("deprecation")
    fun setAirplaneMode(enable: Boolean) {
        val expectedState = if (enable) 1 else 0
        val currentState = Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0)
        Timber.tag("end2end").d("Airplane mode state is currently '$currentState', expected state is '$expectedState'")
        if (expectedState == currentState) return

        device.openQuickSettings()
        tapOnElementWithContentDescription("Vliegtuigmodus")
        context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
    }

    private fun addEvent() {
        tapButton("Menu")
        tapButton("Vaccinatie of test toevoegen")
    }

    fun addVaccinationCertificate() {
        addEvent()
        tapButton("Vaccinatie")
        clickOn("Log in met DigiD")
    }

    fun addRecoveryCertificate() {
        addEvent()
        tapButton("Positieve test")
        clickOn("Log in met DigiD")
    }

    fun addNegativeTestCertificateFromGGD() {
        addEvent()
        scrollTo("Negatieve test")
        tapButton("Negatieve test")
        tapButton("GGD")
        clickOn("Log in met DigiD")
    }

    fun addRetrievedCertificateToApp() {
        waitForText("Kloppen de gegevens?", 30)
        tapButton("Maak bewijs")
        waitForText("Mijn bewijzen", 60)
    }

    fun scrollToBottomOfOverview() {
        waitForView("recyclerView")
        for (i in 3 until 9 step 3) scrollListToPosition(R.id.recyclerView, i)
    }

    fun viewQR(eventType: Event.Type) {
        scrollToBottomOfOverview()
        card(eventType).tapButton("Bekijk QR")
        assertDisplayed("Internationale QR")
    }

    fun viewPreviousQR() {
        clickOn(R.id.previousQrButton)
    }

    fun retrieveCertificateFromServer(bsn: String) {
        if (bsn.isEmpty()) fail("BSN was empty, no certificate can be retrieved.")
        if (authPassword.isEmpty()) fail("Password was empty, no certificate can be retrieved.")

        for (index in 1 until 4) {
            Timber.tag("end2end").d("Log in attempt $index")
            if (!checkForText("Login / Submit", 3)) loginToServer() else break
        }

        enterBsn(bsn)
    }

    private fun loginToServer() {
        if (checkForText("Inloggen") || checkForText("Verificatie vereist")) {
            enterTextInField(0, "coronacheck")
            enterTextInField(1, authPassword)
            tapButtonElement("Inloggen")
        } else {
            acceptChromeOnboarding()
        }
    }

    private fun acceptChromeOnboarding() {
        if (checkForText("Welkom bij Chrome")) tapButtonElement("Accept")
        if (checkForText("Synchronisatie aanzetten?")) tapButtonElement("Nee, bedankt")
        if (checkForText("Inloggen bij Chrome")) tapButtonElement("Nee, bedankt")
        if (checkForText("Toestaan dat Chrome je meldingen stuurt?")) tapButtonElement("Niet toestaan")
        if (checkForText("meldingen")) tapButtonElement("Nee, bedankt")
        if (checkForText("Wil je dat Google Chrome je wachtwoord voor deze site opslaat?")) tapButtonElement("Nooit")
        if (checkForText("Wachtwoord opslaan?")) tapButtonElement("Opslaan")
    }
}
