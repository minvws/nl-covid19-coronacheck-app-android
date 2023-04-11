/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.qrcodes.utils

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import nl.rijksoverheid.ctr.holder.qrcodes.utils.QrCodesFragmentUtilImpl
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import org.junit.Assert.assertFalse
import org.junit.Test

class QrCodesFragmentUtilImplTest {

    @Test
    fun `shouldClose for domestic returns false if credential has not expired`() {
        val now = Clock.fixed(Instant.parse("2021-01-01T10:00:00.00Z"), ZoneId.of("UTC"))
        val expiration = Instant.parse("2021-01-01T11:00:00.00Z")

        val util = QrCodesFragmentUtilImpl(now)
        val shouldClose = util.shouldClose(expiration.epochSecond, GreenCardType.Eu)

        assertFalse(shouldClose)
    }

    @Test
    fun `shouldClose for eu returns false`() {
        val now = Clock.fixed(Instant.parse("2021-01-01T11:00:00.00Z"), ZoneId.of("UTC"))
        val expiration = Instant.parse("2021-01-01T10:00:00.00Z")

        val util = QrCodesFragmentUtilImpl(now)
        val shouldClose = util.shouldClose(expiration.epochSecond, GreenCardType.Eu)

        assertFalse(shouldClose)
    }
}
