/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end

import androidx.test.filters.SdkSuppress
import java.time.LocalDate
import nl.rijksoverheid.ctr.holder.end2end.actions.Add.addNegativeTestCertificateFromGGD
import nl.rijksoverheid.ctr.holder.end2end.actions.Add.addNegativeTestCertificateFromOtherLocation
import nl.rijksoverheid.ctr.holder.end2end.actions.Add.addRecoveryCertificate
import nl.rijksoverheid.ctr.holder.end2end.actions.Add.addRetrievedCertificateToApp
import nl.rijksoverheid.ctr.holder.end2end.actions.Add.addVaccinationCertificate
import nl.rijksoverheid.ctr.holder.end2end.actions.Add.retrieveCertificateWithToken
import nl.rijksoverheid.ctr.holder.end2end.actions.Overview.viewQR
import nl.rijksoverheid.ctr.holder.end2end.actions.QR.viewPreviousQR
import nl.rijksoverheid.ctr.holder.end2end.actions.retrieveCertificateFromServer
import nl.rijksoverheid.ctr.holder.end2end.assertions.Overview.assertInternationalEventIsExpired
import nl.rijksoverheid.ctr.holder.end2end.assertions.Overview.assertInternationalEventOnOverview
import nl.rijksoverheid.ctr.holder.end2end.assertions.Overview.assertInternationalEventWillExpireSoon
import nl.rijksoverheid.ctr.holder.end2end.assertions.Overview.assertQrButtonIsEnabled
import nl.rijksoverheid.ctr.holder.end2end.assertions.QR.assertNoPreviousQR
import nl.rijksoverheid.ctr.holder.end2end.assertions.QR.assertQRisExpired
import nl.rijksoverheid.ctr.holder.end2end.assertions.QR.assertQRisHidden
import nl.rijksoverheid.ctr.holder.end2end.assertions.QR.assertQRisShown
import nl.rijksoverheid.ctr.holder.end2end.assertions.Retrieval.assertRetrievalDetails
import nl.rijksoverheid.ctr.holder.end2end.model.EventType
import nl.rijksoverheid.ctr.holder.end2end.model.NegativeTest
import nl.rijksoverheid.ctr.holder.end2end.model.NegativeToken
import nl.rijksoverheid.ctr.holder.end2end.model.Person
import nl.rijksoverheid.ctr.holder.end2end.model.PositiveTest
import nl.rijksoverheid.ctr.holder.end2end.model.TestEvent.TestType
import nl.rijksoverheid.ctr.holder.end2end.model.VaccinationEvent
import nl.rijksoverheid.ctr.holder.end2end.model.VaccineType
import nl.rijksoverheid.ctr.holder.end2end.model.offsetDays
import nl.rijksoverheid.ctr.holder.end2end.utils.DateTimeUtils
import org.junit.After
import org.junit.Test

@SdkSuppress(minSdkVersion = 33, maxSdkVersion = 33)
class FutureEventQRCodeTest : BaseTest() {

    @After
    fun resetDeviceDate() {
        DateTimeUtils(device).resetDateToAutomatic()
    }

    // region Vaccinations
    @Test
    fun whenDeviceDateIsBeforeExpiry_vaccinationCertificatesAreValid() {
        val person = Person(bsn = "999990020")
        val vac1 = VaccinationEvent(eventDate = today.offsetDays(-60), vaccine = VaccineType.Pfizer)
        val vac2 = VaccinationEvent(eventDate = today.offsetDays(-30), vaccine = VaccineType.Pfizer)
        val deviceDate = today.offsetDays(2)

        addVaccinationCertificate()
        device.retrieveCertificateFromServer(person.bsn)
        addRetrievedCertificateToApp()

        DateTimeUtils(device).setDate(deviceDate)
        relaunchApp()

        assertInternationalEventOnOverview(vac2, dose = "2/2")
        assertInternationalEventOnOverview(vac1, dose = "1/2")
        assertQrButtonIsEnabled(EventType.Vaccination)

        viewQR(EventType.Vaccination)
        assertQRisShown()
        viewPreviousQR()
        assertQRisHidden()
    }

    @Test
    fun whenDeviceDateIsAfterExpiry_vaccinationCertificatesAreExpired() {
        val person = Person(bsn = "999990020")
        val vac1 = VaccinationEvent(eventDate = today.offsetDays(-60), vaccine = VaccineType.Pfizer)
        val vac2 = VaccinationEvent(eventDate = today.offsetDays(-30), vaccine = VaccineType.Pfizer)
        val deviceDate = today.offsetDays(180)

        addVaccinationCertificate()
        device.retrieveCertificateFromServer(person.bsn)
        addRetrievedCertificateToApp()

        DateTimeUtils(device).setDate(deviceDate)
        relaunchApp()

        assertInternationalEventOnOverview(vac2, dose = "2/2")
        assertInternationalEventOnOverview(vac1, dose = "1/2")
        assertQrButtonIsEnabled(EventType.Vaccination)

        viewQR(EventType.Vaccination)
        assertQRisExpired()
        viewPreviousQR()
        assertQRisHidden()
    }

    // endregion
    // region Recovery
    @Test
    fun whenDeviceDateIsBeforeExpiry_recoveryCertificateIsValid() {
        val person = Person(bsn = "999993033")
        val pos = PositiveTest(
            eventDate = today.offsetDays(-30),
            testType = TestType.Pcr,
            validUntil = today.offsetDays(150)
        )

        addRecoveryCertificate()
        device.retrieveCertificateFromServer(person.bsn)
        addRetrievedCertificateToApp()

        DateTimeUtils(device).setDate(today.offsetDays(2))
        relaunchApp()

        assertInternationalEventOnOverview(pos)
        assertQrButtonIsEnabled(EventType.PositiveTest)

        viewQR(EventType.PositiveTest)
        assertQRisShown()
        assertNoPreviousQR()
    }

    @Test
    fun whenDeviceDateIsCloseToExpiry_recoveryCertificatesWillExpireSoon() {
        val person = Person(bsn = "999993033")
        val pos = PositiveTest(
            eventDate = today.offsetDays(-30),
            testType = TestType.Pcr,
            validUntil = today.offsetDays(150)
        )

        addRecoveryCertificate()
        device.retrieveCertificateFromServer(person.bsn)
        addRetrievedCertificateToApp()

        DateTimeUtils(device).setDate(today.offsetDays(140))
        relaunchApp()

        assertInternationalEventOnOverview(pos)
        assertInternationalEventWillExpireSoon(EventType.PositiveTest, daysLeft = 9)
        assertQrButtonIsEnabled(EventType.PositiveTest)

        viewQR(EventType.PositiveTest)
        assertQRisShown()
        assertNoPreviousQR()
    }

    @Test
    fun whenDeviceDateIsAfterExpiry_recoveryCertificateIsRemoved() {
        val person = Person(bsn = "999993033")

        addRecoveryCertificate()
        device.retrieveCertificateFromServer(person.bsn)
        addRetrievedCertificateToApp()

        DateTimeUtils(device).setDate(today.offsetDays(150))
        relaunchApp()

        assertInternationalEventIsExpired(EventType.PositiveTest)
    }

    // endregion
    // region Negative test
    @Test
    fun whenDeviceDateIsBeforeExpiry_negativeTestCertificateIsValid() {
        val person = Person(bsn = "999992004")
        val neg = NegativeTest(eventDate = today, testType = TestType.Pcr)

        addNegativeTestCertificateFromGGD()
        device.retrieveCertificateFromServer(person.bsn)
        addRetrievedCertificateToApp()

        DateTimeUtils(device).setDate(today.offsetDays(2))
        relaunchApp()

        assertInternationalEventOnOverview(neg)
        assertQrButtonIsEnabled(EventType.NegativeTest)

        viewQR(EventType.NegativeTest)
        assertQRisShown()
        assertNoPreviousQR()
    }

    @Test
    fun whenDeviceDateIsAfterExpiry_negativeTestCertificateIsRemoved() {
        val person = Person(bsn = "999992004")

        addNegativeTestCertificateFromGGD()
        device.retrieveCertificateFromServer(person.bsn)
        addRetrievedCertificateToApp()

        DateTimeUtils(device).setDate(today.offsetDays(60))
        relaunchApp()

        assertInternationalEventIsExpired(EventType.NegativeTest)
    }

    // endregion
    // region Tokens
    @Test
    fun whenDeviceDateIsBeforeExpiry_negativeTokenCertificateIsValid() {
        val person = Person(name = "de Beer, Boris", birthDate = LocalDate.of(1971, 7, 31))
        val token = NegativeToken(
            eventDate = today,
            testType = TestType.Pcr,
            couplingCode = "ZZZ-FZB3CUYL55U7ZT-R2"
        )

        addNegativeTestCertificateFromOtherLocation()
        retrieveCertificateWithToken(token.couplingCode)
        assertRetrievalDetails(person, token)
        addRetrievedCertificateToApp()

        DateTimeUtils(device).setDate(today.offsetDays(2))
        relaunchApp()

        assertInternationalEventOnOverview(token)
        assertQrButtonIsEnabled(EventType.NegativeTest)

        viewQR(EventType.NegativeTest)
        assertQRisShown()
        assertNoPreviousQR()
    }

    @Test
    fun whenDeviceDateIsAfterExpiry_negativeTokenCertificateIsRemoved() {
        val person = Person(name = "de Beer, Boris", birthDate = LocalDate.of(1971, 7, 31))
        val token = NegativeToken(
            eventDate = today,
            testType = TestType.Pcr,
            couplingCode = "ZZZ-FZB3CUYL55U7ZT-R2"
        )

        addNegativeTestCertificateFromOtherLocation()
        retrieveCertificateWithToken(token.couplingCode)
        assertRetrievalDetails(person, token)
        addRetrievedCertificateToApp()

        DateTimeUtils(device).setDate(today.offsetDays(60))
        relaunchApp()

        assertInternationalEventIsExpired(EventType.NegativeTest)
    }
    // endregion
}
