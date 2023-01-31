/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end

import androidx.test.filters.SdkSuppress
import java.time.LocalDate
import nl.rijksoverheid.ctr.holder.end2end.model.Event
import nl.rijksoverheid.ctr.holder.end2end.model.NegativeTest
import nl.rijksoverheid.ctr.holder.end2end.model.NegativeToken
import nl.rijksoverheid.ctr.holder.end2end.model.Person
import nl.rijksoverheid.ctr.holder.end2end.model.PositiveTest
import nl.rijksoverheid.ctr.holder.end2end.model.TestEvent.TestType
import nl.rijksoverheid.ctr.holder.end2end.model.VaccinationEvent
import nl.rijksoverheid.ctr.holder.end2end.model.VaccinationEvent.VaccineType
import nl.rijksoverheid.ctr.holder.end2end.model.offsetDays
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.addNegativeTestCertificateFromGGD
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.addNegativeTestCertificateFromOtherLocation
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.addRecoveryCertificate
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.addRetrievedCertificateToApp
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.addVaccinationCertificate
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.retrieveCertificateFromServer
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.retrieveCertificateWithToken
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.viewPreviousQR
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.viewQR
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertInternationalEventIsExpired
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertInternationalEventOnOverview
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertInternationalEventWillExpireSoon
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertNoPreviousQR
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertQRisExpired
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertQRisHidden
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertQRisShown
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertQrButtonIsEnabled
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertRetrievalDetails
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
        retrieveCertificateFromServer(person.bsn)
        addRetrievedCertificateToApp()

        DateTimeUtils(device).setDate(deviceDate)
        relaunchApp()

        assertInternationalEventOnOverview(vac2, dose = "2/2")
        assertInternationalEventOnOverview(vac1, dose = "1/2")
        assertQrButtonIsEnabled(Event.Type.Vaccination)

        viewQR(Event.Type.Vaccination)
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
        retrieveCertificateFromServer(person.bsn)
        addRetrievedCertificateToApp()

        DateTimeUtils(device).setDate(deviceDate)
        relaunchApp()

        assertInternationalEventOnOverview(vac2, dose = "2/2")
        assertInternationalEventOnOverview(vac1, dose = "1/2")
        assertQrButtonIsEnabled(Event.Type.Vaccination)

        viewQR(Event.Type.Vaccination)
        assertQRisExpired()
        viewPreviousQR()
        assertQRisHidden()
    }

    // endregion

    // region Recovery

    @Test
    fun whenDeviceDateIsBeforeExpiry_recoveryCertificateIsValid() {
        val person = Person(bsn = "999993033")
        val pos = PositiveTest(eventDate = today.offsetDays(-30), testType = TestType.Pcr, validUntil = today.offsetDays(150))

        addRecoveryCertificate()
        retrieveCertificateFromServer(person.bsn)
        addRetrievedCertificateToApp()

        DateTimeUtils(device).setDate(today.offsetDays(2))
        relaunchApp()

        assertInternationalEventOnOverview(pos)
        assertQrButtonIsEnabled(Event.Type.PositiveTest)

        viewQR(Event.Type.PositiveTest)
        assertQRisShown()
        assertNoPreviousQR()
    }

    @Test
    fun whenDeviceDateIsCloseToExpiry_recoveryCertificatesWillExpireSoon() {
        val person = Person(bsn = "999993033")
        val pos = PositiveTest(eventDate = today.offsetDays(-30), testType = TestType.Pcr, validUntil = today.offsetDays(150))

        addRecoveryCertificate()
        retrieveCertificateFromServer(person.bsn)
        addRetrievedCertificateToApp()

        DateTimeUtils(device).setDate(today.offsetDays(140))
        relaunchApp()

        assertInternationalEventOnOverview(pos)
        assertInternationalEventWillExpireSoon(Event.Type.PositiveTest, daysLeft = 9)
        assertQrButtonIsEnabled(Event.Type.PositiveTest)

        viewQR(Event.Type.PositiveTest)
        assertQRisShown()
        assertNoPreviousQR()
    }

    @Test
    fun whenDeviceDateIsAfterExpiry_recoveryCertificateIsRemoved() {
        val person = Person(bsn = "999993033")

        addRecoveryCertificate()
        retrieveCertificateFromServer(person.bsn)
        addRetrievedCertificateToApp()

        DateTimeUtils(device).setDate(today.offsetDays(150))
        relaunchApp()

        assertInternationalEventIsExpired(Event.Type.PositiveTest)
    }

    // endregion

    // region Negative test

    @Test
    fun whenDeviceDateIsBeforeExpiry_negativeTestCertificateIsValid() {
        val person = Person(bsn = "999992004")
        val neg = NegativeTest(eventDate = today, testType = TestType.Pcr)

        addNegativeTestCertificateFromGGD()
        retrieveCertificateFromServer(person.bsn)
        addRetrievedCertificateToApp()

        DateTimeUtils(device).setDate(today.offsetDays(2))
        relaunchApp()

        assertInternationalEventOnOverview(neg)
        assertQrButtonIsEnabled(Event.Type.NegativeTest)

        viewQR(Event.Type.NegativeTest)
        assertQRisShown()
        assertNoPreviousQR()
    }

    @Test
    fun whenDeviceDateIsAfterExpiry_negativeTestCertificateIsRemoved() {
        val person = Person(bsn = "999992004")

        addNegativeTestCertificateFromGGD()
        retrieveCertificateFromServer(person.bsn)
        addRetrievedCertificateToApp()

        DateTimeUtils(device).setDate(today.offsetDays(60))
        relaunchApp()

        assertInternationalEventIsExpired(Event.Type.NegativeTest)
    }

    // endregion
}
