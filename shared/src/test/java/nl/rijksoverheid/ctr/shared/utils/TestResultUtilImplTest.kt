package nl.rijksoverheid.ctr.shared.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.*

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class TestResultUtilImplTest {

    private val instant = Instant.ofEpochSecond(10)
    private val clock = Clock.fixed(instant, ZoneId.of("UTC"))
    private val testResultUtil = TestResultUtilImpl(clock)

    @Test
    fun `isValid returns true if current date is after test validity date`() {
        val isValid = testResultUtil.isValid(
            sampleDate = OffsetDateTime.ofInstant(Instant.ofEpochSecond(9), ZoneOffset.UTC),
            validitySeconds = 3
        )
        assertTrue(isValid)
    }

    @Test
    fun `isValid returns false if current date is same as test validity date`() {
        val isValid = testResultUtil.isValid(
            sampleDate = OffsetDateTime.ofInstant(Instant.ofEpochSecond(7), ZoneOffset.UTC),
            validitySeconds = 3
        )
        assertFalse(isValid)
    }

    @Test
    fun `isValid returns false if current date is before test validity date`() {
        val isValid = testResultUtil.isValid(
            sampleDate = OffsetDateTime.ofInstant(Instant.ofEpochSecond(6), ZoneOffset.UTC),
            validitySeconds = 3
        )
        assertFalse(isValid)
    }
}
