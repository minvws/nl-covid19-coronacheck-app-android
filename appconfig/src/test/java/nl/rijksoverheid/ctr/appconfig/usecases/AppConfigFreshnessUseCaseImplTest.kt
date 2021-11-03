/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig.usecases

import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import nl.rijksoverheid.ctr.appconfig.fakeAppConfig
import nl.rijksoverheid.ctr.appconfig.fakeAppConfigPersistenceManager
import nl.rijksoverheid.ctr.appconfig.fakeCachedAppConfigUseCase
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Clock
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
class AppConfigFreshnessUseCaseImplTest {
    private val defaultClock = Clock.systemUTC()

    @Test
    fun `shouldShowConfigWarning returns false if config is not stale yet`() {
        val appConfigFreshnessUseCase = AppConfigFreshnessUseCaseImpl(
            fakeAppConfigPersistenceManager(lastFetchedSeconds = defaultClock.millis() / 1000),
            defaultClock,
            fakeCachedAppConfigUseCase(appConfig = fakeAppConfig(configTtlSeconds = 2419200))
        )
        assertFalse(appConfigFreshnessUseCase.shouldShowConfigFreshnessWarning())
    }

    @Test
    fun `shouldShowConfigWarning returns true if config older than specified config value`() {
        // Config has default value of 300 seconds at time of writing
        val appConfigFreshnessUseCase = AppConfigFreshnessUseCaseImpl(
            fakeAppConfigPersistenceManager(
                lastFetchedSeconds = Clock.offset(
                    defaultClock,
                    Duration.ofSeconds(-301)
                ).millis() / 1000
            ), defaultClock, fakeCachedAppConfigUseCase(appConfig = fakeAppConfig(configTtlSeconds = 2419200))
        )
        assertTrue(appConfigFreshnessUseCase.shouldShowConfigFreshnessWarning())
    }

    @Test
    fun `shouldShowConfigWarning returns false if config just younger than specified config value`() {
        // Config has default value of 300 seconds at time of writing
        val appConfigFreshnessUseCase = AppConfigFreshnessUseCaseImpl(
            fakeAppConfigPersistenceManager(
                lastFetchedSeconds = Clock.offset(
                    defaultClock,
                    Duration.ofSeconds(-299)
                ).millis() / 1000
            ), defaultClock, fakeCachedAppConfigUseCase(appConfig = fakeAppConfig(configTtlSeconds = 2419200))
        )
        assertFalse(appConfigFreshnessUseCase.shouldShowConfigFreshnessWarning())
    }
}