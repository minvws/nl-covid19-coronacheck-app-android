/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end.utils

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
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
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.rest
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.scrollListToPosition
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.scrollTo
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.tapButton
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.tapButtonElement
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.tapOnElementWithContentDescription
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.waitForText
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.writeTo
import nl.rijksoverheid.ctr.holder.end2end.wait.ViewIsShown
import nl.rijksoverheid.ctr.holder.end2end.wait.Wait
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

    // region Adding events

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

    fun addNegativeTestCertificateFromOtherLocation() {
        addEvent()
        scrollTo("Negatieve test")
        tapButton("Negatieve test")
        tapButton("Andere testlocatie")
    }

    fun retrieveCertificateWithToken(retrievalCode: String) {
        assertDisplayed("Testuitslag ophalen")
        writeTo(R.id.unique_code_input, retrievalCode)
        clickOn("Haal testuitslag op")
    }

    fun retrieveCertificateWithTokenAndVerificationCode(retrievalCode: String, verificationCode: String) {
        retrieveCertificateWithToken(retrievalCode)
        writeTo(R.id.verification_code_input, verificationCode)
        clickOn("Haal testuitslag op")
    }

    fun addRetrievedCertificateToApp() {
        waitForText("Kloppen de gegevens?", 30)
        tapButton("Bewijs toevoegen")
        waitForText("Mijn bewijzen", 60)
    }

    // endregion

    // region Overview

    fun scrollToBottomOfOverview() {
        Wait.until(ViewIsShown(onView(withId(R.id.recyclerView)), true))
        for (i in 2 until 12 step 2) scrollListToPosition(R.id.recyclerView, i)
        rest(2)
    }

    fun viewQR(eventType: Event.Type) {
        scrollToBottomOfOverview()
        card(eventType).tapButton("Bekijk QR")
        waitForText("Internationale QR")
    }

    // endregion

    // region QR

    fun viewPreviousQR() {
        clickOn(R.id.previousQrButton)
    }

    // endregion

    // region Private functions

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
        if (checkForText("Accept")) tapButtonElement("Accept")
        if (checkForText("Gebruiken zonder account")) tapButtonElement("Gebruiken zonder account")
        if (checkForText("Synchronisatie aanzetten?")) tapButtonElement("Nee, bedankt")
        if (checkForText("Inloggen bij Chrome")) tapButtonElement("Nee, bedankt")
        if (checkForText("Toestaan dat Chrome je meldingen stuurt?")) tapButtonElement("Niet toestaan")
        if (checkForText("meldingen")) tapButtonElement("Nee, bedankt")
        if (checkForText("Wil je dat Google Chrome je wachtwoord voor deze site opslaat?")) tapButtonElement("Nooit")
        if (checkForText("Wachtwoord opslaan?")) tapButtonElement("Opslaan")
    }

    // endregion
}
