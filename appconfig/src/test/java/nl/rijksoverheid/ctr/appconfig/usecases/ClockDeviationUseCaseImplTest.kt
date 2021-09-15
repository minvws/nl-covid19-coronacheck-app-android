/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig.usecases

import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import nl.rijksoverheid.ctr.appconfig.fakeCachedAppConfigUseCase
import nl.rijksoverheid.ctr.appconfig.persistence.ClockDeviationPersistenceManagerImpl
import nl.rijksoverheid.ctr.shared.SharedApplication
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Clock
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
class ClockDeviationUseCaseImplTest {
    private val clockDeviationPersistenceManager = ClockDeviationPersistenceManagerImpl(
        PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext<SharedApplication>())
    )
    private val defaultClock = Clock.systemUTC()


    @Test
    fun `Clock deviation usecase returns false if clock is correct`() {
         val clockDeviationUseCase = ClockDeviationUseCaseImpl(
            clockDeviationPersistenceManager, defaultClock, fakeCachedAppConfigUseCase()
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
            clockDeviationPersistenceManager, deviatedClock, fakeCachedAppConfigUseCase()
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
            clockDeviationPersistenceManager, deviatedClock, fakeCachedAppConfigUseCase()
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
            clockDeviationPersistenceManager, deviatedClock, fakeCachedAppConfigUseCase()
        )
        clockDeviationUseCase.store(
            serverResponseTimestamp = defaultClock.millis(),
            localReceivedTimestamp = deviatedClock.millis()
        )
        val hasDeviation = clockDeviationUseCase.hasDeviation()
        assertFalse(hasDeviation)
    }
}