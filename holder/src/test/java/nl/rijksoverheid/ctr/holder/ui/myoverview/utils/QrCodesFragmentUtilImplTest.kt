/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.ui.myoverview.utils

import nl.rijksoverheid.ctr.holder.qrcodes.utils.QrCodesFragmentUtilImpl
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class QrCodesFragmentUtilImplTest {

    @Test
    fun `shouldClose returns true if credential has expired`() {
        val now = Clock.fixed(Instant.parse("2021-01-01T11:00:00.00Z"), ZoneId.of("UTC"))
        val expiration = Instant.parse("2021-01-01T10:00:00.00Z")

        val util = QrCodesFragmentUtilImpl(now)
        val shouldClose = util.shouldClose(expiration.epochSecond)

        assertTrue(shouldClose)
    }

    @Test
    fun `shouldClose returns false if credential has not expired`() {
        val now = Clock.fixed(Instant.parse("2021-01-01T10:00:00.00Z"), ZoneId.of("UTC"))
        val expiration = Instant.parse("2021-01-01T11:00:00.00Z")

        val util = QrCodesFragmentUtilImpl(now)
        val shouldClose = util.shouldClose(expiration.epochSecond)

        assertFalse(shouldClose)
    }
}