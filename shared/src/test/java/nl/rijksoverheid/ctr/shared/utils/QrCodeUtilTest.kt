package nl.rijksoverheid.ctr.shared.utils

import nl.rijksoverheid.ctr.shared.util.QrCodeUtil
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class QrCodeUtilTest {

    private val qrCodeUtil = QrCodeUtil()

    @Test
    fun `isValid returns false if difference is more than 3 minutes`() {
        val isValid = qrCodeUtil.isValid(
            currentDate = OffsetDateTime.ofInstant(Instant.ofEpochSecond(60), ZoneOffset.UTC),
            creationDate = OffsetDateTime.ofInstant(Instant.ofEpochSecond(320), ZoneOffset.UTC)
        )
        assertFalse(isValid)
    }

    @Test
    fun `isValid returns true if difference is less than 3 minutes`() {
        val isValid = qrCodeUtil.isValid(
            currentDate = OffsetDateTime.ofInstant(Instant.ofEpochSecond(60), ZoneOffset.UTC),
            creationDate = OffsetDateTime.ofInstant(Instant.ofEpochSecond(180), ZoneOffset.UTC)
        )
        assertTrue(isValid)
    }
}
