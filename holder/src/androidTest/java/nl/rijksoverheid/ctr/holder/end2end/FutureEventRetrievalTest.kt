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
import nl.rijksoverheid.ctr.holder.end2end.actions.retrieveCertificateFromServer
import nl.rijksoverheid.ctr.holder.end2end.assertions.Overview.assertInternationalEventOnOverview
import nl.rijksoverheid.ctr.holder.end2end.assertions.Overview.assertQrButtonIsEnabled
import nl.rijksoverheid.ctr.holder.end2end.assertions.Retrieval.assertRetrievalError
import nl.rijksoverheid.ctr.holder.end2end.model.EventType
import nl.rijksoverheid.ctr.holder.end2end.model.NegativeToken
import nl.rijksoverheid.ctr.holder.end2end.model.Person
import nl.rijksoverheid.ctr.holder.end2end.model.TestType
import nl.rijksoverheid.ctr.holder.end2end.model.offsetDays
import nl.rijksoverheid.ctr.holder.end2end.utils.DateTimeUtils
import org.junit.After
import org.junit.Before
import org.junit.Test

@SdkSuppress(minSdkVersion = 33, maxSdkVersion = 33)
class FutureEventRetrievalTest : BaseTest() {

    @Before
    fun setDeviceDate() {
        DateTimeUtils(device).setDate(today.offsetDays(2))
        relaunchApp()
    }

    @After
    fun resetDeviceDate() {
        DateTimeUtils(device).resetDateToAutomatic()
    }

    @Test
    fun givenDeviceDateInFuture_whenVaccinationIsRetrieved_errorIsDisplayed() {
        addVaccinationCertificate()
        device.retrieveCertificateFromServer(Person().bsn)
        assertRetrievalError("A 210 000 070-9")
    }

    @Test
    fun givenDeviceDateInFuture_whenPositiveTestIsRetrieved_errorIsDisplayed() {
        addRecoveryCertificate()
        device.retrieveCertificateFromServer(Person().bsn)
        assertRetrievalError("A 310 000 070-9")
    }

    @Test
    fun givenDeviceDateInFuture_whenNegativeTestIsRetrieved_errorIsDisplayed() {
        addNegativeTestCertificateFromGGD()
        device.retrieveCertificateFromServer(Person().bsn)
        assertRetrievalError("A 410 000 070-9")
    }

    @Test
    fun givenDeviceDateInFuture_whenNegativeTokenIsRetrieved_dataIsRetrieved() {
        val token = NegativeToken(
            eventDate = today,
            testType = TestType.Pcr,
            couplingCode = "ZZZ-FZB3CUYL55U7ZT-R2"
        )

        addNegativeTestCertificateFromOtherLocation()
        retrieveCertificateWithToken(token.couplingCode)
        addRetrievedCertificateToApp()

        assertInternationalEventOnOverview(token)
        assertQrButtonIsEnabled(EventType.NegativeTest)
    }
}
