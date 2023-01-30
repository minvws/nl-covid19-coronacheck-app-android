/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end

import androidx.test.filters.SdkSuppress
import nl.rijksoverheid.ctr.holder.end2end.model.Event
import nl.rijksoverheid.ctr.holder.end2end.model.NegativeTest
import nl.rijksoverheid.ctr.holder.end2end.model.Person
import nl.rijksoverheid.ctr.holder.end2end.model.PositiveTest
import nl.rijksoverheid.ctr.holder.end2end.model.TestType
import nl.rijksoverheid.ctr.holder.end2end.model.Vaccination
import nl.rijksoverheid.ctr.holder.end2end.model.VaccineType
import nl.rijksoverheid.ctr.holder.end2end.model.offsetDays
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.addNegativeTestCertificateFromGGD
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.addRecoveryCertificate
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.addRetrievedCertificateToApp
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.addVaccinationCertificate
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.retrieveCertificateFromServer
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.viewQR
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertInternationalEventOnOverview
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertInternationalEventWillBecomeValid
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertNoPreviousQR
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertNotYetValidInternationalEventOnOverview
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertQRisShown
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertQrButtonIsDisabled
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertQrButtonIsEnabled
import nl.rijksoverheid.ctr.holder.end2end.utils.DateTimeUtils
import org.junit.After
import org.junit.Test

@SdkSuppress(minSdkVersion = 33, maxSdkVersion = 33)
class PastEventQRCodeTest : BaseTest() {

    @After
    fun resetDeviceDate() {
        DateTimeUtils(device).resetDateToAutomatic()
    }

    @Test
    fun whenDeviceDateIsBeforeEvents_vaccinationCertificatesAreNotYetValid() {
        val person = Person(bsn = "999990020")
        val vac1 = Vaccination(eventDate = today.offsetDays(-60), vaccine = VaccineType.Pfizer)
        val vac2 = Vaccination(eventDate = today.offsetDays(-30), vaccine = VaccineType.Pfizer)
        val deviceDate = today.offsetDays(-80)

        addVaccinationCertificate()
        retrieveCertificateFromServer(person.bsn)
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
        val vac1 = Vaccination(eventDate = today.offsetDays(-60), vaccine = VaccineType.Pfizer)
        val vac2 = Vaccination(eventDate = today.offsetDays(-30), vaccine = VaccineType.Pfizer)
        val deviceDate = today.offsetDays(-35)

        addVaccinationCertificate()
        retrieveCertificateFromServer(person.bsn)
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

    @Test
    fun whenDeviceDateIsBeforeEvent_recoveryCertificateIsNotYetValid() {
        val person = Person(bsn = "999993033")
        val pos = PositiveTest(eventDate = today.offsetDays(-30), testType = TestType.Pcr, validFrom = today.offsetDays(-19), validUntil = today.offsetDays(150))

        addRecoveryCertificate()
        retrieveCertificateFromServer(person.bsn)
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
        retrieveCertificateFromServer(person.bsn)
        addRetrievedCertificateToApp()

        DateTimeUtils(device).setDate(today.offsetDays(-2))
        relaunchApp()

        assertNotYetValidInternationalEventOnOverview(neg)
        assertQrButtonIsDisabled(Event.Type.NegativeTest)
    }
}
