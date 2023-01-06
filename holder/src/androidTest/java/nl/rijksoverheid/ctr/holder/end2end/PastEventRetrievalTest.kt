/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end

import nl.rijksoverheid.ctr.holder.end2end.model.Person
import nl.rijksoverheid.ctr.holder.end2end.model.offsetDays
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.addNegativeTestCertificateFromGGD
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.addRecoveryCertificate
import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.addVaccinationCertificate
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertRetrievalError
import nl.rijksoverheid.ctr.holder.end2end.utils.DateTimeUtils
import org.junit.After
import org.junit.Before
import org.junit.Test

class PastEventRetrievalTest : BaseTest() {

    @Before
    fun setDeviceDate() {
        DateTimeUtils(device).setDate(today.offsetDays(-2))
        restartActivity()
    }

    @After
    fun resetDeviceDate() {
        DateTimeUtils(device).resetDateToAutomatic()
    }

    @Test
    fun givenDeviceDateInPast_whenVaccinationIsRetrieved_errorIsDisplayed() {
        addVaccinationCertificate(Person().bsn)
        assertRetrievalError("A 210 000 070-9")
    }

    @Test
    fun givenDeviceDateInPast_whenPositiveTestIsRetrieved_errorIsDisplayed() {
        addRecoveryCertificate(Person().bsn)
        assertRetrievalError("A 310 000 070-9")
    }

    @Test
    fun givenDeviceDateInPast_whenNegativeTestIsRetrieved_errorIsDisplayed() {
        addNegativeTestCertificateFromGGD(Person().bsn)
        assertRetrievalError("A 410 000 070-9")
    }
}
