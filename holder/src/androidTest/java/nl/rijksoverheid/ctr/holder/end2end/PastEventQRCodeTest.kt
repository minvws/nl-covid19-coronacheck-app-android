/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end

import androidx.test.filters.SdkSuppress
import nl.rijksoverheid.ctr.holder.end2end.actions.Add.addNegativeTestCertificateFromGGD
import nl.rijksoverheid.ctr.holder.end2end.actions.Add.addNegativeTestCertificateFromOtherLocation
import nl.rijksoverheid.ctr.holder.end2end.actions.Add.addRecoveryCertificate
import nl.rijksoverheid.ctr.holder.end2end.actions.Add.addRetrievedCertificateToApp
import nl.rijksoverheid.ctr.holder.end2end.actions.Add.addVaccinationCertificate
import nl.rijksoverheid.ctr.holder.end2end.actions.Add.retrieveCertificateWithToken
import nl.rijksoverheid.ctr.holder.end2end.actions.Overview.viewQR
import nl.rijksoverheid.ctr.holder.end2end.actions.retrieveCertificateFromServer
import nl.rijksoverheid.ctr.holder.end2end.assertions.Overview.assertInternationalEventOnOverview
import nl.rijksoverheid.ctr.holder.end2end.assertions.Overview.assertInternationalEventWillBecomeValid
import nl.rijksoverheid.ctr.holder.end2end.assertions.Overview.assertNotYetValidInternationalEventOnOverview
import nl.rijksoverheid.ctr.holder.end2end.assertions.Overview.assertQrButtonIsDisabled
import nl.rijksoverheid.ctr.holder.end2end.assertions.Overview.assertQrButtonIsEnabled
import nl.rijksoverheid.ctr.holder.end2end.assertions.QR.assertNoPreviousQR
import nl.rijksoverheid.ctr.holder.end2end.assertions.QR.assertQRisShown
import nl.rijksoverheid.ctr.holder.end2end.model.Event
import nl.rijksoverheid.ctr.holder.end2end.model.NegativeTest
import nl.rijksoverheid.ctr.holder.end2end.model.NegativeToken
import nl.rijksoverheid.ctr.holder.end2end.model.Person
import nl.rijksoverheid.ctr.holder.end2end.model.PositiveTest
import nl.rijksoverheid.ctr.holder.end2end.model.TestEvent.TestType
import nl.rijksoverheid.ctr.holder.end2end.model.VaccinationEvent
import nl.rijksoverheid.ctr.holder.end2end.model.VaccinationEvent.VaccineType
import nl.rijksoverheid.ctr.holder.end2end.model.offsetDays
import nl.rijksoverheid.ctr.holder.end2end.utils.DateTimeUtils
import org.junit.After
import org.junit.Test

@SdkSuppress(minSdkVersion = 33, maxSdkVersion = 33)
class PastEventQRCodeTest : BaseTest() {

    @After
    fun resetDeviceDate() {
        DateTimeUtils(device).resetDateToAutomatic()
    }

    // region Vaccinations
    @Test
    fun whenDeviceDateIsBeforeEvents_vaccinationCertificatesAreNotYetValid() {
        val person = Person(bsn = "999990020")
        val vac1 = VaccinationEvent(eventDate = today.offsetDays(-60), vaccine = VaccineType.Pfizer)
        val vac2 = VaccinationEvent(eventDate = today.offsetDays(-30), vaccine = VaccineType.Pfizer)
        val deviceDate = today.offsetDays(-80)

        addVaccinationCertificate()
        device.retrieveCertificateFromServer(person.bsn)
        addRetrievedCertificateToApp()

        DateTimeUtils(device).setDate(deviceDate)
        relaunchApp()

        assertInternationalEventOnOverview(vac2, dose = "2/2")
        assertInternationalEventOnOverview(vac1, dose = "1/2")
        assertInternationalEventWillBecomeValid(Event.Type.Vaccination)
        assertQrButtonIsDisabled(Event.Type.Vaccination)
    }

    @Test
    fun whenDeviceDateIsBetweenEvents_notAllVaccinationsAreValid() {
        val person = Person(bsn = "999990020")
        val vac1 = VaccinationEvent(eventDate = today.offsetDays(-60), vaccine = VaccineType.Pfizer)
        val vac2 = VaccinationEvent(eventDate = today.offsetDays(-30), vaccine = VaccineType.Pfizer)
        val deviceDate = today.offsetDays(-35)

        addVaccinationCertificate()
        device.retrieveCertificateFromServer(person.bsn)
        addRetrievedCertificateToApp()

        DateTimeUtils(device).setDate(deviceDate)
        relaunchApp()

        assertInternationalEventOnOverview(vac2, dose = "2/2")
        assertInternationalEventOnOverview(vac1, dose = "1/2")
        assertInternationalEventWillBecomeValid(Event.Type.Vaccination)
        assertQrButtonIsEnabled(Event.Type.Vaccination)

        viewQR(Event.Type.Vaccination)
        assertQRisShown()
        assertNoPreviousQR()
    }

    // endregion
    @Test
    fun whenDeviceDateIsBeforeEvent_recoveryCertificateIsNotYetValid() {
        val person = Person(bsn = "999993033")
        val pos = PositiveTest(
            eventDate = today.offsetDays(-30),
            testType = TestType.Pcr,
            validFrom = today.offsetDays(-19),
            validUntil = today.offsetDays(150)
        )

        addRecoveryCertificate()
        device.retrieveCertificateFromServer(person.bsn)
        addRetrievedCertificateToApp()

        DateTimeUtils(device).setDate(today.offsetDays(-20))
        relaunchApp()

        assertNotYetValidInternationalEventOnOverview(pos)
        assertQrButtonIsDisabled(Event.Type.PositiveTest)
    }

    @Test
    fun givenDeviceDateBeforeEvent_negativeTestCertificateIsNotYetValid() {
        val person = Person(bsn = "999992004")
        val neg = NegativeTest(eventDate = today, validFrom = today, testType = TestType.Pcr)

        addNegativeTestCertificateFromGGD()
        device.retrieveCertificateFromServer(person.bsn)
        addRetrievedCertificateToApp()

        DateTimeUtils(device).setDate(today.offsetDays(-2))
        relaunchApp()

        assertNotYetValidInternationalEventOnOverview(neg)
        assertQrButtonIsDisabled(Event.Type.NegativeTest)
    }

    @Test
    fun givenDeviceDateBeforeEvent_negativeTokenCertificateIsNotYetValid() {
        val token = NegativeToken(
            eventDate = today,
            validFrom = today,
            testType = TestType.Pcr,
            couplingCode = "ZZZ-FZB3CUYL55U7ZT-R2"
        )

        addNegativeTestCertificateFromOtherLocation()
        retrieveCertificateWithToken(token.couplingCode)
        addRetrievedCertificateToApp()

        DateTimeUtils(device).setDate(today.offsetDays(-2))
        relaunchApp()

        assertNotYetValidInternationalEventOnOverview(token)
        assertQrButtonIsDisabled(Event.Type.NegativeTest)
    }
}
