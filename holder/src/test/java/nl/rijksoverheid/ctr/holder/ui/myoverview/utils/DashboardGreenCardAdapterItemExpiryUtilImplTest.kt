package nl.rijksoverheid.ctr.holder.ui.myoverview.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import nl.rijksoverheid.ctr.holder.dashboard.items.DashboardGreenCardAdapterItemExpiryUtil
import nl.rijksoverheid.ctr.holder.dashboard.items.DashboardGreenCardAdapterItemExpiryUtilImpl
import nl.rijksoverheid.ctr.holder.fakeOriginEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
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
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "en-land")
class DashboardGreenCardAdapterItemExpiryUtilImplTest : AutoCloseKoinTest() {
    
    private val context: Context by lazy {
        ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `getExpireCountdownText returns Hide when expire date more than 24 hours away for vaccination`() {
        val util = DashboardGreenCardAdapterItemExpiryUtilImpl(
            clock = Clock.fixed(Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC")),
            context = context
        )
        val result = util.getExpireCountdown(
            expireDate = OffsetDateTime.ofInstant(
                Instant.parse("2021-01-02T00:01:00.00Z"),
                ZoneId.of("UTC")
            ),
            type = OriginType.Vaccination
        )
        assertEquals(DashboardGreenCardAdapterItemExpiryUtil.ExpireCountDown.Hide, result)
    }

    @Test
    fun `getExpireCountdownText returns Show when expire date less than 24 hours away for vaccination`() {
        val util = DashboardGreenCardAdapterItemExpiryUtilImpl(
            clock = Clock.fixed(Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC")),
            context = context
        )
        val result = util.getExpireCountdown(
            expireDate = OffsetDateTime.ofInstant(
                Instant.parse("2021-01-01T23:50:00.00Z"),
                ZoneId.of("UTC")
            ),
            type = OriginType.Vaccination
        )
        assertEquals(
            DashboardGreenCardAdapterItemExpiryUtil.ExpireCountDown.Show(
                hoursLeft = 23,
                minutesLeft = 50
            ), result
        )
    }

    @Test
    fun `getExpireCountdownText returns Hide when expire date more than 24 hours away for recovery`() {
        val util = DashboardGreenCardAdapterItemExpiryUtilImpl(
            clock = Clock.fixed(Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC")),
            context = context
        )
        val result = util.getExpireCountdown(
            expireDate = OffsetDateTime.ofInstant(
                Instant.parse("2021-01-02T00:01:00.00Z"),
                ZoneId.of("UTC")
            ),
            type = OriginType.Recovery
        )
        assertEquals(DashboardGreenCardAdapterItemExpiryUtil.ExpireCountDown.Hide, result)
    }

    @Test
    fun `getExpireCountdownText returns Show when expire date less than 24 hours away for recovery`() {
        val util = DashboardGreenCardAdapterItemExpiryUtilImpl(
            clock = Clock.fixed(Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC")),
            context = context
        )
        val result = util.getExpireCountdown(
            expireDate = OffsetDateTime.ofInstant(
                Instant.parse("2021-01-01T23:59:00.00Z"),
                ZoneId.of("UTC")
            ),
            type = OriginType.Recovery
        )
        assertEquals(
            DashboardGreenCardAdapterItemExpiryUtil.ExpireCountDown.Show(
                hoursLeft = 23,
                minutesLeft = 59
            ), result
        )
    }

    @Test
    fun `getExpireCountdownText returns Hide when expire date more than 6 hours away for test`() {
        val util = DashboardGreenCardAdapterItemExpiryUtilImpl(
            clock = Clock.fixed(Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC")),
            context = context
        )
        val result = util.getExpireCountdown(
            expireDate = OffsetDateTime.ofInstant(
                Instant.parse("2021-01-01T06:01:00.00Z"),
                ZoneId.of("UTC")
            ),
            type = OriginType.Test
        )
        assertEquals(DashboardGreenCardAdapterItemExpiryUtil.ExpireCountDown.Hide, result)
    }

    @Test
    fun `getExpireCountdownText returns Show when expire date less than 6 hours away for test`() {
        val util = DashboardGreenCardAdapterItemExpiryUtilImpl(
            clock = Clock.fixed(Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC")),
            context = context
        )
        val result = util.getExpireCountdown(
            expireDate = OffsetDateTime.ofInstant(
                Instant.parse("2021-01-01T05:59:00.00Z"),
                ZoneId.of("UTC")
            ),
            type = OriginType.Test
        )
        assertEquals(
            DashboardGreenCardAdapterItemExpiryUtil.ExpireCountDown.Show(
                hoursLeft = 5,
                minutesLeft = 59
            ), result
        )
    }

    @Test
    fun `getExpireCountdownText returned minutes should never be below 1`() {
        val util = DashboardGreenCardAdapterItemExpiryUtilImpl(
            clock = Clock.fixed(Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC")),
            context = context
        )
        val result = util.getExpireCountdown(
            expireDate = OffsetDateTime.ofInstant(
                Instant.parse("2021-01-01T00:00:50.00Z"),
                ZoneId.of("UTC")
            ),
            type = OriginType.Vaccination
        )
        assertEquals(
            DashboardGreenCardAdapterItemExpiryUtil.ExpireCountDown.Show(
                hoursLeft = 0,
                minutesLeft = 1
            ), result
        )
    }

    @Test
    fun `getExpiryText should return text of only minutes if it's less than an hour`() {
        
        val util = DashboardGreenCardAdapterItemExpiryUtilImpl(
            clock = Clock.fixed(Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC")),
            context = context
        )

        val result = DashboardGreenCardAdapterItemExpiryUtil.ExpireCountDown.Show(0, 15)

        val actual = util.getExpiryText(result)

        assertEquals("Expires in 15 minutes", actual)
    }

    @Test
    fun `getExpiryText should return text of hours and minutes`() {
        
        val util = DashboardGreenCardAdapterItemExpiryUtilImpl(
            clock = Clock.fixed(Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC")),
            context = context
        )

        val result = DashboardGreenCardAdapterItemExpiryUtil.ExpireCountDown.Show(2, 15)

        val actual = util.getExpiryText(result)

        assertEquals("Expires in 2 hours, 15 minutes", actual)
    }

    @Test
    fun `getExpiryText should return text of hour and minutes`() {

        val util = DashboardGreenCardAdapterItemExpiryUtilImpl(
            clock = Clock.fixed(Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC")),
            context = context
        )

        val result = DashboardGreenCardAdapterItemExpiryUtil.ExpireCountDown.Show(1, 15)

        val actual = util.getExpiryText(result)

        assertEquals("Expires in 1 hour, 15 minutes", actual)
    }

    @Test
    fun `getExpiryText should return text of hours and minute`() {

        val util = DashboardGreenCardAdapterItemExpiryUtilImpl(
            clock = Clock.fixed(Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC")),
            context = context
        )

        val result = DashboardGreenCardAdapterItemExpiryUtil.ExpireCountDown.Show(2, 1)

        val actual = util.getExpiryText(result)

        assertEquals("Expires in 2 hours, 1 minute", actual)
    }

    @Test
    fun `getExpiryText should return text of hour and minute`() {

        val util = DashboardGreenCardAdapterItemExpiryUtilImpl(
            clock = Clock.fixed(Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC")),
            context = context
        )

        val result = DashboardGreenCardAdapterItemExpiryUtil.ExpireCountDown.Show(1, 1)

        val actual = util.getExpiryText(result)

        assertEquals("Expires in 1 hour, 1 minute", actual)
    }

    @Test
    @Config(qualifiers = "nl-land")
    fun `getExpiryText should return text of uur and minuut in dutch`() {

        val util = DashboardGreenCardAdapterItemExpiryUtilImpl(
            clock = Clock.fixed(Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC")),
            context = context
        )

        val result = DashboardGreenCardAdapterItemExpiryUtil.ExpireCountDown.Show(1, 1)

        val actual = util.getExpiryText(result)

        assertEquals("Verloopt over 1 uur en 1 minuut", actual)
    }

    @Test
    fun `Get the last valid origin when it's the only valid one left`() {
        
        val util = DashboardGreenCardAdapterItemExpiryUtilImpl(
            clock = Clock.fixed(Instant.parse("2021-01-10T00:00:00.00Z"), ZoneId.of("UTC")),
            context = context
        )
        val validOrigin = fakeOriginEntity(
            expirationTime = OffsetDateTime.ofInstant(
                Instant.parse("2021-01-11T00:00:00.00Z"), ZoneId.of("UTC")
            )
        )
        val origins = listOf(
            validOrigin,
            fakeOriginEntity(
                expirationTime = OffsetDateTime.ofInstant(
                    Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC")
                )
            )
        )

        assertEquals(validOrigin, util.getLastValidOrigin(origins))
    }

    @Test
    fun `Last valid origin should not return anything when more than 1 origins are valid`() {
        
        val util = DashboardGreenCardAdapterItemExpiryUtilImpl(
            clock = Clock.fixed(Instant.parse("2021-01-10T00:00:00.00Z"), ZoneId.of("UTC")),
            context = context
        )
        val validOrigin = fakeOriginEntity(
            expirationTime = OffsetDateTime.ofInstant(
                Instant.parse("2021-01-11T00:00:00.00Z"), ZoneId.of("UTC")
            )
        )
        val origins = listOf(
            validOrigin,
            fakeOriginEntity(
                expirationTime = OffsetDateTime.ofInstant(
                    Instant.parse("2021-01-12T00:00:00.00Z"), ZoneId.of("UTC")
                )
            )
        )

        assertEquals(null, util.getLastValidOrigin(origins))
    }

    @Test
    fun `Last valid origin should not return anything when none are valid`() {
        
        val util = DashboardGreenCardAdapterItemExpiryUtilImpl(
            clock = Clock.fixed(Instant.parse("2021-01-10T00:00:00.00Z"), ZoneId.of("UTC")),
            context = context
        )
        val validOrigin = fakeOriginEntity(
            expirationTime = OffsetDateTime.ofInstant(
                Instant.parse("2021-01-09T00:00:00.00Z"), ZoneId.of("UTC")
            )
        )
        val origins = listOf(
            validOrigin,
            fakeOriginEntity(
                expirationTime = OffsetDateTime.ofInstant(
                    Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC")
                )
            )
        )

        assertEquals(null, util.getLastValidOrigin(origins))
    }
}

