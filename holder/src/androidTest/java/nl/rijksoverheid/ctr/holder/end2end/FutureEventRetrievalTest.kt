/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end

import androidx.test.filters.SdkSuppress
import nl.rijksoverheid.ctr.holder.end2end.model.Person
import nl.rijksoverheid.ctr.holder.end2end.model.offsetDays
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.addNegativeTestCertificateFromGGD
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.addRecoveryCertificate
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.addVaccinationCertificate
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.retrieveCertificateFromServer
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertRetrievalError
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
        retrieveCertificateFromServer(Person().bsn)
        assertRetrievalError("A 210 000 070-9")
    }

    @Test
    fun givenDeviceDateInFuture_whenPositiveTestIsRetrieved_errorIsDisplayed() {
        addRecoveryCertificate()
        retrieveCertificateFromServer(Person().bsn)
        assertRetrievalError("A 310 000 070-9")
    }

    @Test
    fun givenDeviceDateInFuture_whenNegativeTestIsRetrieved_errorIsDisplayed() {
        addNegativeTestCertificateFromGGD()
        retrieveCertificateFromServer(Person().bsn)
        assertRetrievalError("A 410 000 070-9")
    }
}
