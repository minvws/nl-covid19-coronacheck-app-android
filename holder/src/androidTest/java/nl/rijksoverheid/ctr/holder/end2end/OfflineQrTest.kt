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
import nl.rijksoverheid.ctr.holder.end2end.actions.QR.viewPreviousQR
import nl.rijksoverheid.ctr.holder.end2end.actions.retrieveCertificateFromServer
import nl.rijksoverheid.ctr.holder.end2end.actions.setAirplaneMode
import nl.rijksoverheid.ctr.holder.end2end.assertions.QR.assertNoPreviousQR
import nl.rijksoverheid.ctr.holder.end2end.assertions.QR.assertQRisHidden
import nl.rijksoverheid.ctr.holder.end2end.assertions.QR.assertQRisShown
import nl.rijksoverheid.ctr.holder.end2end.model.EventType
import nl.rijksoverheid.ctr.holder.end2end.model.NegativeToken
import nl.rijksoverheid.ctr.holder.end2end.model.Person
import nl.rijksoverheid.ctr.holder.end2end.model.TestEvent
import org.junit.After
import org.junit.Test

@SdkSuppress(minSdkVersion = 33, maxSdkVersion = 33)
class OfflineQrTest : BaseTest() {

    @After
    fun goOnline() {
        instrumentation.setAirplaneMode(false)
    }

    @Test
    fun whenDeviceIsOffline_vaccinationCertificateShowsQR() {
        val person = Person(bsn = "999990020")

        addVaccinationCertificate()
        device.retrieveCertificateFromServer(person.bsn)
        addRetrievedCertificateToApp()

        instrumentation.setAirplaneMode(true)
        relaunchApp()

        viewQR(EventType.Vaccination)
        assertQRisShown()
        viewPreviousQR()
        assertQRisHidden()
        assertNoPreviousQR()
    }

    @Test
    fun whenDeviceIsOffline_positiveTestCertificateShowsQR() {
        val person = Person(bsn = "999993033")

        addRecoveryCertificate()
        device.retrieveCertificateFromServer(person.bsn)
        addRetrievedCertificateToApp()

        instrumentation.setAirplaneMode(true)
        relaunchApp()

        viewQR(EventType.PositiveTest)
        assertQRisShown()
        assertNoPreviousQR()
    }

    @Test
    fun whenDeviceIsOffline_negativeTestCertificateShowsQr() {
        val person = Person(bsn = "999992004")

        addNegativeTestCertificateFromGGD()
        device.retrieveCertificateFromServer(person.bsn)
        addRetrievedCertificateToApp()

        instrumentation.setAirplaneMode(true)
        relaunchApp()

        viewQR(EventType.NegativeTest)
        assertQRisShown()
        assertNoPreviousQR()
    }

    @Test
    fun whenDeviceIsOffline_negativeTokenCertificateShowsQr() {
        val token = NegativeToken(
            eventDate = today,
            testType = TestEvent.TestType.Pcr,
            couplingCode = "ZZZ-FZB3CUYL55U7ZT-R2"
        )

        addNegativeTestCertificateFromOtherLocation()
        retrieveCertificateWithToken(token.couplingCode)
        addRetrievedCertificateToApp()

        instrumentation.setAirplaneMode(true)
        relaunchApp()

        viewQR(EventType.NegativeTest)
        assertQRisShown()
        assertNoPreviousQR()
    }
}
