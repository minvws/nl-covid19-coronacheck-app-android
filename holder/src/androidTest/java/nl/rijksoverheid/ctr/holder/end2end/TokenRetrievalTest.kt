/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end

import androidx.test.filters.SdkSuppress
import java.time.LocalDate
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.end2end.actions.Add.addNegativeTestCertificateFromOtherLocation
import nl.rijksoverheid.ctr.holder.end2end.actions.Add.addRetrievedCertificateToApp
import nl.rijksoverheid.ctr.holder.end2end.actions.Add.retrieveCertificateWithToken
import nl.rijksoverheid.ctr.holder.end2end.actions.Add.retrieveCertificateWithTokenAndVerificationCode
import nl.rijksoverheid.ctr.holder.end2end.actions.Overview.viewQR
import nl.rijksoverheid.ctr.holder.end2end.assertions.Overview.assertInternationalEventOnOverview
import nl.rijksoverheid.ctr.holder.end2end.assertions.Overview.assertQrButtonIsEnabled
import nl.rijksoverheid.ctr.holder.end2end.assertions.QR.assertInternationalQRDetails
import nl.rijksoverheid.ctr.holder.end2end.assertions.QR.assertNoPreviousQR
import nl.rijksoverheid.ctr.holder.end2end.assertions.Retrieval.assertRetrievalDetails
import nl.rijksoverheid.ctr.holder.end2end.interaction.Barista.assertContains
import nl.rijksoverheid.ctr.holder.end2end.interaction.Barista.assertDisplayed
import nl.rijksoverheid.ctr.holder.end2end.interaction.Barista.assertNotContains
import nl.rijksoverheid.ctr.holder.end2end.interaction.Barista.assertNotExist
import nl.rijksoverheid.ctr.holder.end2end.interaction.Barista.clickOn
import nl.rijksoverheid.ctr.holder.end2end.interaction.Barista.writeTo
import nl.rijksoverheid.ctr.holder.end2end.interaction.waitUntilTextIsShown
import nl.rijksoverheid.ctr.holder.end2end.model.EventType
import nl.rijksoverheid.ctr.holder.end2end.model.NegativeToken
import nl.rijksoverheid.ctr.holder.end2end.model.Person
import nl.rijksoverheid.ctr.holder.end2end.model.TestType
import nl.rijksoverheid.ctr.holder.end2end.utils.RunFirebaseTestOnDemand
import org.junit.Test

@SdkSuppress(minSdkVersion = 33, maxSdkVersion = 33)
@RunFirebaseTestOnDemand
class TokenTest : BaseTest() {

    private val person = Person(name = "de Beer, Boris", birthDate = LocalDate.of(1971, 7, 31))

    @Test
    fun retrieveTokenWithVerificationCode_assertOverviewAndQRDetails() {
        val token = NegativeToken(
            eventDate = today,
            testType = TestType.Pcr,
            couplingCode = "ZZZ-ZT66URU6TY2J96-32",
            verificationCode = "123456"
        )

        addNegativeTestCertificateFromOtherLocation()
        retrieveCertificateWithTokenAndVerificationCode(
            token.couplingCode,
            token.verificationCode!!
        )
        assertRetrievalDetails(person, token)
        addRetrievedCertificateToApp()

        assertInternationalEventOnOverview(token)
        assertQrButtonIsEnabled(EventType.NegativeTest)

        viewQR()
        assertInternationalQRDetails(person, token)
        assertNoPreviousQR()
    }

    @Test
    fun retrieveTokenWithoutVerificationCode_assertOverviewAndQRDetails() {
        val token = NegativeToken(
            eventDate = today,
            testType = TestType.Pcr,
            couplingCode = "ZZZ-FZB3CUYL55U7ZT-R2"
        )

        addNegativeTestCertificateFromOtherLocation()
        retrieveCertificateWithToken(token.couplingCode)
        assertRetrievalDetails(person, token)
        addRetrievedCertificateToApp()

        assertInternationalEventOnOverview(token)
        assertQrButtonIsEnabled(EventType.NegativeTest)

        viewQR()
        assertInternationalQRDetails(person, token)
        assertNoPreviousQR()
    }

    @Test
    fun retrieveTokens_verifyErrors() {
        val token = NegativeToken(
            eventDate = today,
            testType = TestType.Pcr,
            couplingCode = "ZZZ-ZT66URU6TY2J96-32",
            verificationCode = "123456"
        )

        addNegativeTestCertificateFromOtherLocation()

        // Assert screen
        assertDisplayed("Testuitslag ophalen")
        assertNotExist("Deze code is niet geldig.")

        // Assert info sheet
        assertDisplayed("Heb je geen ophaalcode?")
        clickOn("Heb je geen ophaalcode?")
        assertContains("Je krijgt van de testlocatie een ophaalcode met cijfers en letters.")
        assertContains("Heb je geen code gekregen? Of ben je deze kwijtgeraakt? Neem dan contact op met je testlocatie.")
        clickOn(R.id.close)

        // No retrieval code
        writeTo(R.id.unique_code_text, "")
        clickOn("Haal testuitslag op")
        assertDisplayed("Graag eerst een ophaalcode invullen")

        // Incorrect retrieval code
        writeTo(R.id.unique_code_text, "incorrect")
        clickOn("Haal testuitslag op")
        assertContains("Deze code is niet geldig.")

        // Correct retrieval code
        writeTo(R.id.unique_code_text, token.couplingCode)
        clickOn("Haal testuitslag op")
        assertNotContains("Deze code is niet geldig.")
        waitUntilTextIsShown("Je krijgt een code via sms of e-mail")

        // No verification code
        writeTo(R.id.verification_code_text, "")
        clickOn("Haal testuitslag op")
        waitUntilTextIsShown("Graag eerst een verificatiecode invullen")

        // Incorrect verification code
        writeTo(R.id.verification_code_text, "incorrect")
        clickOn("Haal testuitslag op")
        waitUntilTextIsShown("Geen geldige combinatie. Vul de 6-cijferige verificatiecode in.")

        // Assert info dialog
        clickOn("Geen verificatiecode gekregen?")
        assertDisplayed("Geen verificatiecode gekregen?")
        assertDisplayed("Je krijgt de verificatiecode via een sms of e-mail. Niks gekregen? Klik hieronder op ‘stuur opnieuw’ voor een nieuwe code.")
        clickOn("Sluiten")

        // Correct verification code
        writeTo(R.id.verification_code_text, token.verificationCode!!)
        clickOn("Haal testuitslag op")
        assertNotExist("Geen geldige combinatie. Vul de 6-cijferige verificatiecode in.")
    }
}
