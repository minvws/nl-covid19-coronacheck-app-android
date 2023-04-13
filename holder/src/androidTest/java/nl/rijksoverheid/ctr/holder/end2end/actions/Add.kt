/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end.actions

import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.end2end.actions.MenuItems.addEvent
import nl.rijksoverheid.ctr.holder.end2end.interaction.Barista.assertDisplayed
import nl.rijksoverheid.ctr.holder.end2end.interaction.Barista.clickBack
import nl.rijksoverheid.ctr.holder.end2end.interaction.Barista.clickOn
import nl.rijksoverheid.ctr.holder.end2end.interaction.Barista.scrollTo
import nl.rijksoverheid.ctr.holder.end2end.interaction.Barista.writeTo
import nl.rijksoverheid.ctr.holder.end2end.interaction.Espresso.tapButtonPosition
import nl.rijksoverheid.ctr.holder.end2end.interaction.waitUntilButtonEnabled
import nl.rijksoverheid.ctr.holder.end2end.interaction.waitUntilTextIsShown

object Add {

    fun addRecoveryCertificate() {
        addEvent()
        clickOn("Positieve test")
        clickOn("Log in met DigiD")
    }

    fun addNegativeTestCertificateFromGGD() {
        addEvent()
        scrollTo("Negatieve test")
        clickOn("Negatieve test")
        clickOn("GGD")
        clickOn("Log in met DigiD")
    }

    fun addNegativeTestCertificateFromOtherLocation() {
        addEvent()
        scrollTo("Negatieve test")
        clickOn("Negatieve test")
        clickOn("Andere testlocatie")
    }

    fun retrieveCertificateWithToken(retrievalCode: String) {
        assertDisplayed("Testuitslag ophalen")
        writeTo(R.id.unique_code_input, retrievalCode)
        clickOn("Haal testuitslag op")
    }

    fun retrieveCertificateWithTokenAndVerificationCode(
        retrievalCode: String,
        verificationCode: String
    ) {
        retrieveCertificateWithToken(retrievalCode)
        waitUntilTextIsShown("Geen verificatiecode gekregen?")
        writeTo(R.id.verification_code_input, verificationCode)
        clickOn("Haal testuitslag op")
    }

    fun addVaccinationCertificate(combinedWithPositiveTest: Boolean = false) {
        addEvent()
        clickOn("Vaccinatie")
        if (combinedWithPositiveTest) clickOn(R.id.checkbox)
        clickOn("Log in met DigiD")
    }

    fun addRetrievedCertificateToApp(endScreen: EndScreen? = null, replace: Boolean? = null) {
        waitUntilTextIsShown("Kloppen de gegevens?", 30)
        waitUntilButtonEnabled("Bewijs toevoegen")
        tapButtonPosition("Bewijs toevoegen", 0)
        endScreen?.let {
            waitUntilTextIsShown(endScreen.message)
            clickOn("Naar mijn bewijzen")
        }
        if (replace != null) replaceExistingCertificate(replace)
        waitUntilTextIsShown("Mijn bewijzen", 60)
    }

    enum class EndScreen(val message: String) {
        VaccinationAndRecoveryEventCreated("Vaccinatiebewijs en herstelbewijs gemaakt"),
        OnlyInternationalEventCreated("Er is alleen een internationaal bewijs gemaakt")
    }

    private fun replaceExistingCertificate(replace: Boolean) {
        waitUntilTextIsShown("Wil je je bewijs vervangen?")
        if (replace) {
            clickOn("Vervang")
        } else {
            clickOn("Stoppen")
            clickBack()
            clickBack()
            clickBack()
        }
    }
}
