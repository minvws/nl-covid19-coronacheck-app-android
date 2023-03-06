/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end

import androidx.test.filters.SdkSuppress
import java.time.LocalDate
import nl.rijksoverheid.ctr.holder.end2end.actions.Add
import nl.rijksoverheid.ctr.holder.end2end.actions.Add.addNegativeTestCertificateFromGGD
import nl.rijksoverheid.ctr.holder.end2end.actions.Add.addNegativeTestCertificateFromOtherLocation
import nl.rijksoverheid.ctr.holder.end2end.actions.Add.addRecoveryCertificate
import nl.rijksoverheid.ctr.holder.end2end.actions.Add.addRetrievedCertificateToApp
import nl.rijksoverheid.ctr.holder.end2end.actions.Add.addVaccinationCertificate
import nl.rijksoverheid.ctr.holder.end2end.actions.Add.retrieveCertificateWithToken
import nl.rijksoverheid.ctr.holder.end2end.actions.MenuItems.deleteItemFromWallet
import nl.rijksoverheid.ctr.holder.end2end.actions.MenuItems.returnFromWalletToOverview
import nl.rijksoverheid.ctr.holder.end2end.actions.MenuItems.viewWallet
import nl.rijksoverheid.ctr.holder.end2end.actions.retrieveCertificateFromServer
import nl.rijksoverheid.ctr.holder.end2end.assertions.MenuItems.assertAmountOfWalletItemsPerSection
import nl.rijksoverheid.ctr.holder.end2end.assertions.MenuItems.assertNoEventsInWallet
import nl.rijksoverheid.ctr.holder.end2end.assertions.MenuItems.assertWalletItem
import nl.rijksoverheid.ctr.holder.end2end.assertions.Overview.assertNoCertificatesOnOverview
import nl.rijksoverheid.ctr.holder.end2end.model.NegativeToken
import nl.rijksoverheid.ctr.holder.end2end.model.Person
import nl.rijksoverheid.ctr.holder.end2end.model.PositiveTest
import nl.rijksoverheid.ctr.holder.end2end.model.TestType
import nl.rijksoverheid.ctr.holder.end2end.model.VaccinationEvent
import nl.rijksoverheid.ctr.holder.end2end.model.VaccineType
import nl.rijksoverheid.ctr.holder.end2end.model.offsetDays
import org.junit.Test

@SdkSuppress(minSdkVersion = 33, maxSdkVersion = 33)
class WalletTest : BaseTest() {

    // region Retrieval

    @Test
    fun retrieveVaccination_verifyWalletData() {
        val person = Person(bsn = "999990032")
        val vac0 = VaccinationEvent(eventDate = today.offsetDays(-30), VaccineType.Pfizer)
        val vac1 = VaccinationEvent(eventDate = today.offsetDays(-60), VaccineType.Pfizer)
        val vac2 = VaccinationEvent(eventDate = today.offsetDays(-90), VaccineType.Pfizer)

        addVaccinationCertificate()
        device.retrieveCertificateFromServer(person.bsn)
        addRetrievedCertificateToApp()

        viewWallet()
        assertWalletItem(person, vac0)
        assertWalletItem(person, vac1)
        assertWalletItem(person, vac2)
    }

    @Test
    fun retrieveVaccinationAndRecovery_verifyWalletData() {
        val person = Person(bsn = "999993495")
        val vac = VaccinationEvent(eventDate = today.offsetDays(-30), vaccine = VaccineType.Pfizer)
        val pos = PositiveTest(eventDate = today.offsetDays(-60), testType = TestType.Pcr)

        addVaccinationCertificate(combinedWithPositiveTest = true)
        device.retrieveCertificateFromServer(person.bsn)
        addRetrievedCertificateToApp(Add.EndScreen.VaccinationAndRecoveryEventCreated)

        viewWallet()
        assertWalletItem(person, pos)
        assertWalletItem(person, vac)
    }

    @Test
    fun retrieveMultipleEvents_verifyEncodingAndWalletData() {
        val person = Person(bsn = "999992739", name = "吹牛, 鲍勃")
        val vac0 = VaccinationEvent(eventDate = today.offsetDays(-30), VaccineType.Pfizer)
        val vac1 = VaccinationEvent(eventDate = today.offsetDays(-60), VaccineType.Pfizer)
        val pos = PositiveTest(eventDate = today.offsetDays(-90), testType = TestType.Pcr)

        addVaccinationCertificate(combinedWithPositiveTest = true)
        device.retrieveCertificateFromServer(person.bsn)
        addRetrievedCertificateToApp(Add.EndScreen.VaccinationAndRecoveryEventCreated)

        viewWallet()
        assertWalletItem(person, pos)
        assertWalletItem(person, vac0)
        assertWalletItem(person, vac1)
    }

    @Test
    fun retrieveMultipleVaccinations_whenSetupIsNotReplaced_WalletShowsSetup() {
        val setup = Person(bsn = "999993562")
        val vac0 = VaccinationEvent(eventDate = today.offsetDays(-60), VaccineType.Pfizer)
        val vac1 = VaccinationEvent(eventDate = today.offsetDays(-90), VaccineType.Pfizer)

        val person = Person("999994190")

        addVaccinationCertificate()
        device.retrieveCertificateFromServer(setup.bsn)
        addRetrievedCertificateToApp()

        addVaccinationCertificate()
        device.retrieveCertificateFromServer(person.bsn)
        addRetrievedCertificateToApp(replace = false)

        viewWallet()
        assertAmountOfWalletItemsPerSection(arrayOf(2))
        assertWalletItem(person, vac0)
        assertWalletItem(person, vac1)
    }

    @Test
    fun retrieveMultipleVaccinations_whenSetupIsReplaced_WalletShowsReplacedData() {
        val setup = Person(bsn = "999993562")

        val person = Person(bsn = "999994190", "de Heuvel, Pieter", LocalDate.of(1970, 2, 2))
        val vac = VaccinationEvent(eventDate = today.offsetDays(-30), VaccineType.Janssen)

        addVaccinationCertificate()
        device.retrieveCertificateFromServer(setup.bsn)
        addRetrievedCertificateToApp()

        addVaccinationCertificate()
        device.retrieveCertificateFromServer(person.bsn)
        addRetrievedCertificateToApp(replace = true)

        viewWallet()
        assertAmountOfWalletItemsPerSection(arrayOf(1))
        assertWalletItem(person, vac)
    }

    // endregion

    // region Removal

    @Test
    fun removeVaccinationFromWallet() {
        val person = Person(bsn = "999990081")
        addVaccinationCertificate()
        device.retrieveCertificateFromServer(person.bsn)
        addRetrievedCertificateToApp()

        viewWallet()
        deleteItemFromWallet()
        assertNoEventsInWallet()

        returnFromWalletToOverview()
        assertNoCertificatesOnOverview()
    }

    @Test
    fun removePositiveTestFromWallet() {
        val person = Person(bsn = "999993033")
        addRecoveryCertificate()
        device.retrieveCertificateFromServer(person.bsn)
        addRetrievedCertificateToApp()

        viewWallet()
        deleteItemFromWallet()
        assertNoEventsInWallet()

        returnFromWalletToOverview()
        assertNoCertificatesOnOverview()
    }

    @Test
    fun removeNegativeTestFromWallet() {
        val person = Person(bsn = "999992004")
        addNegativeTestCertificateFromGGD()
        device.retrieveCertificateFromServer(person.bsn)
        addRetrievedCertificateToApp()

        viewWallet()
        deleteItemFromWallet()
        assertNoEventsInWallet()

        returnFromWalletToOverview()
        assertNoCertificatesOnOverview()
    }

    @Test
    fun removeNegativeTokenFromWallet() {
        val token = NegativeToken(
            eventDate = today,
            testType = TestType.Pcr,
            couplingCode = "ZZZ-FZB3CUYL55U7ZT-R2"
        )

        addNegativeTestCertificateFromOtherLocation()
        retrieveCertificateWithToken(token.couplingCode)
        addRetrievedCertificateToApp()

        viewWallet()
        deleteItemFromWallet()
        assertNoEventsInWallet()

        returnFromWalletToOverview()
        assertNoCertificatesOnOverview()
    }

    @Test
    fun removeMultipleEventsFromWallet() {
        val person = Person(bsn = "999991346")
        addVaccinationCertificate(combinedWithPositiveTest = true)
        device.retrieveCertificateFromServer(person.bsn)
        addRetrievedCertificateToApp(Add.EndScreen.VaccinationAndRecoveryEventCreated)

        viewWallet()
        assertAmountOfWalletItemsPerSection(arrayOf(1, 1))

        deleteItemFromWallet(position = 0)
        assertAmountOfWalletItemsPerSection(arrayOf(1))

        deleteItemFromWallet()
        assertNoEventsInWallet()

        returnFromWalletToOverview()
        assertNoCertificatesOnOverview()
    }

    // endregion
}
