package nl.rijksoverheid.ctr.verifier.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.*
import java.util.concurrent.TimeUnit

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class QrCodeUtilImplTest {

    private val instant = Instant.ofEpochSecond(TimeUnit.MINUTES.toSeconds(6))
    private val clock = Clock.fixed(instant, ZoneId.of("UTC"))
    private val qrCodeUtil = QrCodeUtilImpl(clock)

    @Test
    fun `isValid returns false if difference is more than 3 minutes`() {
        val isValid = qrCodeUtil.isValid(
            creationDate = OffsetDateTime.ofInstant(
                Instant.ofEpochSecond(
                    TimeUnit.MINUTES.toSeconds(
                        2
                    )
                ), ZoneOffset.UTC
            ),
            isPaperProof = "0"
        )
        assertFalse(isValid)
    }

    @Test
    fun `isValid always returns true if isPaperProof is 1`() {
        val isValid = qrCodeUtil.isValid(
            creationDate = OffsetDateTime.ofInstant(
                Instant.ofEpochSecond(
                    TimeUnit.MINUTES.toSeconds(
                        2
                    )
                ), ZoneOffset.UTC
            ),
            isPaperProof = "1"
        )
        assertTrue(isValid)
    }

    @Test
    fun `isValid returns true if difference is less than 3 minutes`() {
        val isValid = qrCodeUtil.isValid(
            creationDate = OffsetDateTime.ofInstant(
                Instant.ofEpochSecond(
                    TimeUnit.MINUTES.toSeconds(
                        4
                    )
                ), ZoneOffset.UTC
            ),
            isPaperProof = "0"
        )
        assertTrue(isValid)
    }
}
