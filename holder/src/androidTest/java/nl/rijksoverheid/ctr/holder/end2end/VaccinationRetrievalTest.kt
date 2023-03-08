/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end

import androidx.test.filters.SdkSuppress
import nl.rijksoverheid.ctr.holder.end2end.actions.Add.addRetrievedCertificateToApp
import nl.rijksoverheid.ctr.holder.end2end.actions.Add.addVaccinationCertificate
import nl.rijksoverheid.ctr.holder.end2end.actions.Overview.viewQR
import nl.rijksoverheid.ctr.holder.end2end.actions.QR.viewPreviousQR
import nl.rijksoverheid.ctr.holder.end2end.actions.retrieveCertificateFromServer
import nl.rijksoverheid.ctr.holder.end2end.assertions.Overview.assertInternationalEventOnOverview
import nl.rijksoverheid.ctr.holder.end2end.assertions.Overview.assertQrButtonIsEnabled
import nl.rijksoverheid.ctr.holder.end2end.assertions.QR.assertInternationalQRDetails
import nl.rijksoverheid.ctr.holder.end2end.assertions.QR.assertNoPreviousQR
import nl.rijksoverheid.ctr.holder.end2end.assertions.QR.assertQRisHidden
import nl.rijksoverheid.ctr.holder.end2end.assertions.QR.assertQRisShown
import nl.rijksoverheid.ctr.holder.end2end.assertions.Retrieval.assertRetrievalDetails
import nl.rijksoverheid.ctr.holder.end2end.model.EventType
import nl.rijksoverheid.ctr.holder.end2end.model.Person
import nl.rijksoverheid.ctr.holder.end2end.model.VaccinationEvent
import nl.rijksoverheid.ctr.holder.end2end.model.VaccineType
import nl.rijksoverheid.ctr.holder.end2end.model.offsetDays
import org.junit.Test

@SdkSuppress(minSdkVersion = 33, maxSdkVersion = 33)
class VaccinationRetrievalTest : BaseTest() {

    @Test
    fun retrieveVaccinationWith2Pfizer_assertOverviewAndQRDetails() {
        val person = Person(bsn = "999990020")
        val vac1 = VaccinationEvent(eventDate = today.offsetDays(-60), vaccine = VaccineType.Pfizer)
        val vac2 = VaccinationEvent(eventDate = today.offsetDays(-30), vaccine = VaccineType.Pfizer)

        addVaccinationCertificate()
        device.retrieveCertificateFromServer(person.bsn)
        assertRetrievalDetails(person, vac2, position = 0)
        assertRetrievalDetails(person, vac1, position = 1)
        addRetrievedCertificateToApp()

        assertInternationalEventOnOverview(vac2, dose = "2/2")
        assertInternationalEventOnOverview(vac1, dose = "1/2")
        assertQrButtonIsEnabled(EventType.Vaccination)

        viewQR()
        assertQRisShown()
        assertInternationalQRDetails(person, vac2, dose = "2/2")
        viewPreviousQR()
        assertQRisHidden()
        assertInternationalQRDetails(person, vac1, dose = "1/2")
        assertNoPreviousQR()
    }

    @Test
    fun retrieveVaccinationWith2Moderna_assertOverviewAndQRDetails() {
        val person = Person("999990159")
        val vac1 =
            VaccinationEvent(eventDate = today.offsetDays(-60), vaccine = VaccineType.Moderna)
        val vac2 =
            VaccinationEvent(eventDate = today.offsetDays(-30), vaccine = VaccineType.Moderna)

        addVaccinationCertificate()
        device.retrieveCertificateFromServer(person.bsn)
        assertRetrievalDetails(person, vac2, position = 0)
        assertRetrievalDetails(person, vac1, position = 1)
        addRetrievedCertificateToApp()

        assertInternationalEventOnOverview(vac2, dose = "2/2")
        assertInternationalEventOnOverview(vac1, dose = "1/2")
        assertQrButtonIsEnabled(EventType.Vaccination)

        viewQR()
        assertQRisShown()
        assertInternationalQRDetails(person, vac2, dose = "2/2")
        viewPreviousQR()
        assertQRisHidden()
        assertInternationalQRDetails(person, vac1, dose = "1/2")
        assertNoPreviousQR()
    }

    @Test
    fun retrieveVaccinationWith2Janssen_assertOverviewAndQRDetails() {
        val person = Person("999990093")
        val vac1 =
            VaccinationEvent(eventDate = today.offsetDays(-60), vaccine = VaccineType.Janssen)
        val vac2 =
            VaccinationEvent(eventDate = today.offsetDays(-30), vaccine = VaccineType.Janssen)

        addVaccinationCertificate()
        device.retrieveCertificateFromServer(person.bsn)
        assertRetrievalDetails(person, vac2, position = 0)
        assertRetrievalDetails(person, vac1, position = 1)
        addRetrievedCertificateToApp()

        assertInternationalEventOnOverview(vac2, dose = "2/1")
        assertInternationalEventOnOverview(vac1, dose = "1/1")
        assertQrButtonIsEnabled(EventType.Vaccination)

        viewQR()
        assertQRisShown()
        assertInternationalQRDetails(person, vac2, dose = "2/1")
        viewPreviousQR()
        assertQRisShown()
        assertInternationalQRDetails(person, vac1, dose = "1/1")
        assertNoPreviousQR()
    }
}
