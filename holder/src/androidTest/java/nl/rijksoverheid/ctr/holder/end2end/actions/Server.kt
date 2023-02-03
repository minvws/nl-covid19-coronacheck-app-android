/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end.actions

import androidx.test.uiautomator.UiDevice
import junit.framework.TestCase
import nl.rijksoverheid.ctr.holder.end2end.BaseTest.Companion.authPassword
import nl.rijksoverheid.ctr.holder.end2end.interaction.checkForText
import nl.rijksoverheid.ctr.holder.end2end.interaction.enterBsn
import nl.rijksoverheid.ctr.holder.end2end.interaction.enterTextInField
import nl.rijksoverheid.ctr.holder.end2end.interaction.tapButtonElement
import timber.log.Timber

fun UiDevice.retrieveCertificateFromServer(bsn: String) {
    if (authPassword.isEmpty()) TestCase.fail("Password was empty, no certificate can be retrieved.")
    if (bsn.isEmpty()) TestCase.fail("BSN was empty, no certificate can be retrieved.")

    for (index in 1 until 4) {
        Timber.tag("end2end").d("Log in attempt $index")
        if (!checkForText("Login / Submit", 3)) loginToServer(authPassword) else break
    }

    enterBsn(bsn)
}

private fun UiDevice.loginToServer(password: String) {
    if (checkForText("Inloggen") || checkForText("Verificatie vereist")) {
        enterTextInField(0, "coronacheck")
        enterTextInField(1, password)
        tapButtonElement("Inloggen")
    } else {
        acceptChromeOnboarding()
    }
}

private fun UiDevice.acceptChromeOnboarding() {
    if (checkForText("Accept")) tapButtonElement("Accept")
    if (checkForText("Gebruiken zonder account")) tapButtonElement("Gebruiken zonder account")
    if (checkForText("Synchronisatie aanzetten?")) tapButtonElement("Nee, bedankt")
    if (checkForText("Inloggen bij Chrome")) tapButtonElement("Nee, bedankt")
    if (checkForText("Toestaan dat Chrome je meldingen stuurt?")) tapButtonElement("Niet toestaan")
    if (checkForText("meldingen")) tapButtonElement("Nee, bedankt")
    if (checkForText("Wil je dat Google Chrome je wachtwoord voor deze site opslaat?")) tapButtonElement("Nooit")
    if (checkForText("Wachtwoord opslaan?")) tapButtonElement("Opslaan")
}
