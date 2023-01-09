/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end

import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.addNegativeTestCertificateFromGGD
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.addRecoveryCertificate
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.addVaccinationCertificate
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.setAirplaneMode
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertSomethingWentWrong
import org.junit.After
import org.junit.Before
import org.junit.Test

class OfflineEventRetrievalTest : BaseTest() {

    @Before
    fun goOffline() {
        setAirplaneMode(true)
        launchApp()
    }

    @After
    fun goOnline() {
        setAirplaneMode(false)
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
}
