/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig.usecases

import nl.rijksoverheid.ctr.appconfig.fakeCachedAppConfigUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
class ClockDeviationUseCaseImplTest {
    private val defaultClock = Clock.systemUTC()
    private val fixedClock = Clock.fixed(
        Instant.parse("2021-11-02T18:00:00.00Z"),
        ZoneId.systemDefault()
    )

    @Test
    fun `Clock deviation usecase returns false if clock is correct`() {
         val clockDeviationUseCase = ClockDeviationUseCaseImpl(
            defaultClock, fakeCachedAppConfigUseCase()
        )
        clockDeviationUseCase.store(
            serverResponseTimestamp = defaultClock.millis(),
            localReceivedTimestamp = defaultClock.millis()
        )
        val hasDeviation = clockDeviationUseCase.hasDeviation()
        assertFalse(hasDeviation)
    }

    @Test
    fun `Clock deviation usecase returns true if clock is off by more than threshold in the future`() {
        val deviatedClock = Clock.offset(defaultClock, Duration.ofMinutes(10L))
        val clockDeviationUseCase = ClockDeviationUseCaseImpl(
            deviatedClock, fakeCachedAppConfigUseCase()
        )
        clockDeviationUseCase.store(
            serverResponseTimestamp = defaultClock.millis(),
            localReceivedTimestamp = deviatedClock.millis()
        )
        val hasDeviation = clockDeviationUseCase.hasDeviation()
        assertTrue(hasDeviation)
    }

    @Test
    fun `Clock deviation usecase returns true if clock is off by more than threshold in the past`() {
        val deviatedClock = Clock.offset(defaultClock, Duration.ofMinutes(-10L))
        val clockDeviationUseCase = ClockDeviationUseCaseImpl(
            deviatedClock, fakeCachedAppConfigUseCase()
        )
        clockDeviationUseCase.store(
            serverResponseTimestamp = defaultClock.millis(),
            localReceivedTimestamp = deviatedClock.millis()
        )
        val hasDeviation = clockDeviationUseCase.hasDeviation()
        assertTrue(hasDeviation)
    }

    @Test
    fun `Clock deviation usecase returns false if clock is off by less than threshold`() {
        val deviatedClock = Clock.offset(defaultClock, Duration.ofSeconds(10L))
        val clockDeviationUseCase = ClockDeviationUseCaseImpl(
            deviatedClock, fakeCachedAppConfigUseCase()
        )
        clockDeviationUseCase.store(
            serverResponseTimestamp = defaultClock.millis(),
            localReceivedTimestamp = deviatedClock.millis()
        )
        val hasDeviation = clockDeviationUseCase.hasDeviation()
        assertFalse(hasDeviation)
    }

    @Test
    fun `Clock deviation usecase returns adjusted clock`() {
        // device clock is 10 seconds ahead of server time
        val deviceClock = Clock.offset(fixedClock, Duration.ofSeconds(10L))
        val clockDeviationUseCase = ClockDeviationUseCaseImpl(
            deviceClock, fakeCachedAppConfigUseCase()
        )
        clockDeviationUseCase.store(
            serverResponseTimestamp = fixedClock.millis(),
            localReceivedTimestamp = deviceClock.millis()
        )

        val adjustedClock = clockDeviationUseCase.getAdjustedClock(deviceClock)
        assertEquals(fixedClock.instant(), adjustedClock.instant())
    }
}