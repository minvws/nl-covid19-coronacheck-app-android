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
class MyOverviewGreenCardAdapterUtilImplTest {

    @Test
    fun `getExpireCountdownText returns Hide when expire date more than 24 hours away`() {
        val util = MyOverviewGreenCardAdapterUtilImpl(
            clock = Clock.fixed(Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC"))
        )
        val result = util.getExpireCountdownText(
            expireDate = OffsetDateTime.ofInstant(
                Instant.parse("2021-01-02T07:00:00.00Z"),
                ZoneId.of("UTC")
            )
        )
        assertEquals(MyOverviewGreenCardAdapterUtil.ExpireCountDown.Hide, result)
    }

    @Test
    fun `getExpireCountdownText returns Show when expire date less than 24 hours away`() {
        val util = MyOverviewGreenCardAdapterUtilImpl(
            clock = Clock.fixed(Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC"))
        )
        val result = util.getExpireCountdownText(
            expireDate = OffsetDateTime.ofInstant(
                Instant.parse("2021-01-01T23:50:00.00Z"),
                ZoneId.of("UTC")
            )
        )
        assertEquals(
            MyOverviewGreenCardAdapterUtil.ExpireCountDown.Show(
                hoursLeft = 23,
                minutesLeft = 50
            ), result
        )
    }

    @Test
    fun `getExpireCountdownText returned minutes should never be below 1`() {
        val util = MyOverviewGreenCardAdapterUtilImpl(
            clock = Clock.fixed(Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC"))
        )
        val result = util.getExpireCountdownText(
            expireDate = OffsetDateTime.ofInstant(
                Instant.parse("2021-01-01T00:00:50.00Z"),
                ZoneId.of("UTC")
            )
        )
        assertEquals(
            MyOverviewGreenCardAdapterUtil.ExpireCountDown.Show(
                hoursLeft = 0,
                minutesLeft = 1
            ), result
        )
    }
}

