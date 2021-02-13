package nl.rijksoverheid.ctr.shared.utils

import nl.rijksoverheid.ctr.shared.util.TestResultUtil
import org.junit.Assert
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
class TestResultUtilTest {

    private val testResultUtil = TestResultUtil()

    @Test
    fun `isValid returns true if current date is after test validity date`() {
        val isValid = testResultUtil.isValid(
            currentDate = OffsetDateTime.ofInstant(Instant.ofEpochSecond(10), ZoneOffset.UTC),
            sampleDate = OffsetDateTime.ofInstant(Instant.ofEpochSecond(9), ZoneOffset.UTC),
            validitySeconds = 3
        )
        Assert.assertTrue(isValid)
    }

    @Test
    fun `isValid returns false if current date is same as test validity date`() {
        val isValid = testResultUtil.isValid(
            currentDate = OffsetDateTime.ofInstant(Instant.ofEpochSecond(10), ZoneOffset.UTC),
            sampleDate = OffsetDateTime.ofInstant(Instant.ofEpochSecond(7), ZoneOffset.UTC),
            validitySeconds = 3
        )
        Assert.assertFalse(isValid)
    }

    @Test
    fun `isValid returns false if current date is before test validity date`() {
        val isValid = testResultUtil.isValid(
            currentDate = OffsetDateTime.ofInstant(Instant.ofEpochSecond(10), ZoneOffset.UTC),
            sampleDate = OffsetDateTime.ofInstant(Instant.ofEpochSecond(6), ZoneOffset.UTC),
            validitySeconds = 3
        )
        Assert.assertFalse(isValid)
    }
}
