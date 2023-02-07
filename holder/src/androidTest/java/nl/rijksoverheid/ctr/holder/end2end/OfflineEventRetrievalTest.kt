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
import nl.rijksoverheid.ctr.holder.end2end.actions.Add.addVaccinationCertificate
import nl.rijksoverheid.ctr.holder.end2end.actions.Add.retrieveCertificateWithToken
import nl.rijksoverheid.ctr.holder.end2end.actions.setAirplaneMode
import nl.rijksoverheid.ctr.holder.end2end.assertions.Retrieval.assertSomethingWentWrong
import nl.rijksoverheid.ctr.holder.end2end.model.NegativeToken
import nl.rijksoverheid.ctr.holder.end2end.model.TestType
import org.junit.After
import org.junit.Before
import org.junit.Test

@SdkSuppress(minSdkVersion = 33, maxSdkVersion = 33)
class OfflineEventRetrievalTest : BaseTest() {

    @Before
    fun goOffline() {
        instrumentation.setAirplaneMode(true)
        relaunchApp()
    }

    @After
    fun goOnline() {
        instrumentation.setAirplaneMode(false)
    }

    @Test
    fun givenDeviceIsOffline_whenVaccinationIsRetrieved_errorIsDisplayed() {
        addVaccinationCertificate()
        assertSomethingWentWrong()
    }

    @Test
    fun givenDeviceIsOffline_whenPositiveTestIsRetrieved_errorIsDisplayed() {
        addRecoveryCertificate()
        assertSomethingWentWrong()
    }

    @Test
    fun givenDeviceIsOffline_whenNegativeTestIsRetrieved_errorIsDisplayed() {
        addNegativeTestCertificateFromGGD()
        assertSomethingWentWrong()
    }

    @Test
    fun givenDeviceIsOffline_whenTokenIsRetrieved_errorIsDisplayed() {
        val token = NegativeToken(eventDate = today, testType = TestType.Pcr, couplingCode = "ZZZ-FZB3CUYL55U7ZT-R2")

        addNegativeTestCertificateFromOtherLocation()
        retrieveCertificateWithToken(token.couplingCode)
        assertSomethingWentWrong()
    }
}
