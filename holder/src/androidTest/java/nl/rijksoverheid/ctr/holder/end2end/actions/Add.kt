/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end.actions

import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.end2end.interaction.Barista.assertDisplayed
import nl.rijksoverheid.ctr.holder.end2end.interaction.Barista.clickOn
import nl.rijksoverheid.ctr.holder.end2end.interaction.Barista.scrollTo
import nl.rijksoverheid.ctr.holder.end2end.interaction.Barista.writeTo
import nl.rijksoverheid.ctr.holder.end2end.interaction.Espresso.tapButton
import nl.rijksoverheid.ctr.holder.end2end.interaction.waitUntilTextIsShown

object Add {

    private fun addEvent() {
        tapButton("Menu")
        tapButton("Vaccinatie of test toevoegen")
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
        tapButton("Vaccinatie")
        if (combinedWithPositiveTest) clickOn(R.id.checkbox)
        clickOn("Log in met DigiD")
    }

    fun addRetrievedCertificateToApp(endScreen: String? = null) {
        waitUntilTextIsShown("Kloppen de gegevens?", 30)
        tapButton("Bewijs toevoegen")
        endScreen?.let {
            waitUntilTextIsShown(it)
            tapButton("Naar mijn bewijzen")
        }
        waitUntilTextIsShown("Mijn bewijzen", 60)
    }
}
