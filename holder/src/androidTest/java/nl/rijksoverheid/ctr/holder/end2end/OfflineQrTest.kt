/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end

import androidx.test.filters.SdkSuppress
import nl.rijksoverheid.ctr.holder.end2end.model.Event
import nl.rijksoverheid.ctr.holder.end2end.model.Person
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.addRetrievedCertificateToApp
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.addVaccinationCertificate
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.retrieveCertificateFromServer
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.setAirplaneMode
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.viewPreviousQR
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.viewQR
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertNoPreviousQR
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertQRisHidden
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertQRisShown
import org.junit.After
import org.junit.Test

@SdkSuppress(minSdkVersion = 33, maxSdkVersion = 33)
class OfflineQrTest : BaseTest() {

    @After
    fun goOnline() {
        setAirplaneMode(false)
    }

    @Test
    fun whenDeviceIsOffline_vaccinationCertificateShowsQR() {
        val person = Person(bsn = "999990020")

        addVaccinationCertificate()
        retrieveCertificateFromServer(person.bsn)
        addRetrievedCertificateToApp()

        setAirplaneMode(true)
        launchApp()

        viewQR(Event.Type.Vaccination)
        assertQRisShown()
        viewPreviousQR()
        assertQRisHidden()
        assertNoPreviousQR()
    }

    @Test
    fun whenDeviceIsOffline_positiveTestCertificateShowsQR() {
        val person = Person(bsn = "999993033")

        Actions.addRecoveryCertificate()
        retrieveCertificateFromServer(person.bsn)
        addRetrievedCertificateToApp()

        setAirplaneMode(true)
        launchApp()

        viewQR(Event.Type.PositiveTest)
        assertQRisShown()
        assertNoPreviousQR()
    }

    @Test
    fun whenDeviceIsOffline_negativeTestCertificateShowsQr() {
        val person = Person(bsn = "999992004")

        Actions.addNegativeTestCertificateFromGGD()
        retrieveCertificateFromServer(person.bsn)
        addRetrievedCertificateToApp()

        setAirplaneMode(true)
        launchApp()

        viewQR(Event.Type.NegativeTest)
        assertQRisShown()
        assertNoPreviousQR()
    }
}
