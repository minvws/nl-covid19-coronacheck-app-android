/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end

import androidx.test.filters.SdkSuppress
import nl.rijksoverheid.ctr.holder.end2end.actions.Add.addNegativeTestCertificateFromGGD
import nl.rijksoverheid.ctr.holder.end2end.actions.Add.addRecoveryCertificate
import nl.rijksoverheid.ctr.holder.end2end.actions.Add.addRetrievedCertificateToApp
import nl.rijksoverheid.ctr.holder.end2end.actions.Add.addVaccinationCertificate
import nl.rijksoverheid.ctr.holder.end2end.actions.Overview.viewQR
import nl.rijksoverheid.ctr.holder.end2end.actions.retrieveCertificateFromServer
import nl.rijksoverheid.ctr.holder.end2end.assertions.Overview.assertInternationalEventOnOverview
import nl.rijksoverheid.ctr.holder.end2end.assertions.Overview.assertQrButtonIsEnabled
import nl.rijksoverheid.ctr.holder.end2end.assertions.QR.assertInternationalQRDetails
import nl.rijksoverheid.ctr.holder.end2end.assertions.QR.assertNoPreviousQR
import nl.rijksoverheid.ctr.holder.end2end.assertions.QR.assertQRisShown
import nl.rijksoverheid.ctr.holder.end2end.assertions.Retrieval.assertRetrievalDetails
import nl.rijksoverheid.ctr.holder.end2end.model.Event
import nl.rijksoverheid.ctr.holder.end2end.model.NegativeTest
import nl.rijksoverheid.ctr.holder.end2end.model.Person
import nl.rijksoverheid.ctr.holder.end2end.model.PositiveTest
import nl.rijksoverheid.ctr.holder.end2end.model.TestEvent.TestType
import nl.rijksoverheid.ctr.holder.end2end.model.VaccinationEvent
import nl.rijksoverheid.ctr.holder.end2end.model.offsetDays
import org.junit.Test

@SdkSuppress(minSdkVersion = 33, maxSdkVersion = 33)
class TestsRetrievalTest : BaseTest() {

    @Test
    fun retrievePositiveTest_assertOverviewAndQRDetails() {
        val person = Person(bsn = "999993033")
        val pos = PositiveTest(
            eventDate = today.offsetDays(-30),
            testType = TestType.Pcr,
            validUntil = today.offsetDays(150)
        )

        addRecoveryCertificate()
        device.retrieveCertificateFromServer(person.bsn)
        assertRetrievalDetails(person, pos)
        addRetrievedCertificateToApp()

        assertInternationalEventOnOverview(pos)
        assertQrButtonIsEnabled(Event.Type.PositiveTest)

        viewQR(Event.Type.PositiveTest)
        assertQRisShown()
        assertInternationalQRDetails(person, pos)
        assertNoPreviousQR()
    }

    @Test
    fun retrieveNegativeTest_assertOverviewAndQRDetails() {
        val person = Person(bsn = "999992004")
        val neg = NegativeTest(eventDate = today, testType = TestType.Pcr)

        addNegativeTestCertificateFromGGD()
        device.retrieveCertificateFromServer(person.bsn)
        assertRetrievalDetails(person, neg)
        addRetrievedCertificateToApp()

        assertInternationalEventOnOverview(neg)
        assertQrButtonIsEnabled(Event.Type.NegativeTest)

        viewQR(Event.Type.NegativeTest)
        assertQRisShown()
        assertInternationalQRDetails(person, neg)
        assertNoPreviousQR()
    }

    @Test
    fun retrievePositiveTestAndVaccination_assertEndScreen() {
        val person = Person(bsn = "999991772")
        val vac1 =
            VaccinationEvent(today.offsetDays(-90), vaccine = VaccinationEvent.VaccineType.Pfizer)
        val vac2 =
            VaccinationEvent(today.offsetDays(-120), vaccine = VaccinationEvent.VaccineType.Pfizer)
        val pos = PositiveTest(
            eventDate = today.offsetDays(-30),
            testType = TestType.Pcr,
            validUntil = today.offsetDays(150)
        )

        addVaccinationCertificate(combinedWithPositiveTest = true)
        device.retrieveCertificateFromServer(person.bsn)
        assertRetrievalDetails(person, pos, 0)
        assertRetrievalDetails(person, vac1, 1)
        assertRetrievalDetails(person, vac2, 2)
        addRetrievedCertificateToApp("Vaccinatiebewijs en herstelbewijs gemaakt")

        assertInternationalEventOnOverview(vac2, dose = "2/2")
        assertInternationalEventOnOverview(vac1, dose = "1/2")
        assertInternationalEventOnOverview(pos)
        assertQrButtonIsEnabled(Event.Type.Vaccination)
        assertQrButtonIsEnabled(Event.Type.PositiveTest)
    }
}
