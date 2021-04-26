package nl.rijksoverheid.ctr.holder.ui.myoverview.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class TestResultAdapterItemUtilImplTest {

    @Test
    fun `getExpireCountdownText returns Hide when expire date more than 6 hours away`() {
        val util = TestResultAdapterItemUtilImpl(
            clock = Clock.fixed(Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC"))
        )
        val result = util.getExpireCountdownText(
            expireDate = OffsetDateTime.ofInstant(
                Instant.parse("2021-01-01T07:00:00.00Z"),
                ZoneId.of("UTC")
            )
        )
        assertEquals(TestResultAdapterItemUtil.ExpireCountDown.Hide, result)
    }

    @Test
    fun `getExpireCountdownText returns Show when expire date more less than 6 hours away`() {
        val util = TestResultAdapterItemUtilImpl(
            clock = Clock.fixed(Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC"))
        )
        val result = util.getExpireCountdownText(
            expireDate = OffsetDateTime.ofInstant(
                Instant.parse("2021-01-01T05:50:00.00Z"),
                ZoneId.of("UTC")
            )
        )
        assertEquals(
            TestResultAdapterItemUtil.ExpireCountDown.Show(
                hoursLeft = 5,
                minutesLeft = 50
            ), result
        )
    }

    @Test
    fun `getExpireCountdownText returned minutes should never be below 1`() {
        val util = TestResultAdapterItemUtilImpl(
            clock = Clock.fixed(Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC"))
        )
        val result = util.getExpireCountdownText(
            expireDate = OffsetDateTime.ofInstant(
                Instant.parse("2021-01-01T00:00:50.00Z"),
                ZoneId.of("UTC")
            )
        )
        assertEquals(
            TestResultAdapterItemUtil.ExpireCountDown.Show(
                hoursLeft = 0,
                minutesLeft = 1
            ), result
        )
    }
}

