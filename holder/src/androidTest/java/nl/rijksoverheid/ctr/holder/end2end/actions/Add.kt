/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end.actions

import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions
import com.adevinta.android.barista.interaction.BaristaClickInteractions
import com.adevinta.android.barista.interaction.BaristaEditTextInteractions
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.end2end.interaction.scrollTo
import nl.rijksoverheid.ctr.holder.end2end.interaction.tapButton
import nl.rijksoverheid.ctr.holder.end2end.interaction.waitUntilTextIsShown

object Add {

    private fun addEvent() {
        tapButton("Menu")
        tapButton("Vaccinatie of test toevoegen")
    }

    fun addRecoveryCertificate() {
        addEvent()
        tapButton("Positieve test")
        BaristaClickInteractions.clickOn("Log in met DigiD")
    }

    fun addNegativeTestCertificateFromGGD() {
        addEvent()
        scrollTo("Negatieve test")
        tapButton("Negatieve test")
        tapButton("GGD")
        BaristaClickInteractions.clickOn("Log in met DigiD")
    }

    fun addNegativeTestCertificateFromOtherLocation() {
        addEvent()
        scrollTo("Negatieve test")
        tapButton("Negatieve test")
        tapButton("Andere testlocatie")
    }

    fun retrieveCertificateWithToken(retrievalCode: String) {
        BaristaVisibilityAssertions.assertDisplayed("Testuitslag ophalen")
        BaristaEditTextInteractions.writeTo(R.id.unique_code_input, retrievalCode)
        BaristaClickInteractions.clickOn("Haal testuitslag op")
    }

    fun retrieveCertificateWithTokenAndVerificationCode(
        retrievalCode: String,
        verificationCode: String
    ) {
        retrieveCertificateWithToken(retrievalCode)
        BaristaEditTextInteractions.writeTo(R.id.verification_code_input, verificationCode)
        BaristaClickInteractions.clickOn("Haal testuitslag op")
    }

    fun addVaccinationCertificate() {
        addEvent()
        tapButton("Vaccinatie")
        BaristaClickInteractions.clickOn("Log in met DigiD")
    }

    fun addRetrievedCertificateToApp() {
        waitUntilTextIsShown("Kloppen de gegevens?", 30)
        tapButton("Bewijs toevoegen")
        waitUntilTextIsShown("Mijn bewijzen", 60)
    }
}
