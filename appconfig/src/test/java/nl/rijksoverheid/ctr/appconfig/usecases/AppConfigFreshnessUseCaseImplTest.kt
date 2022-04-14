/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig.usecases

import nl.rijksoverheid.ctr.appconfig.fakeAppConfig
import nl.rijksoverheid.ctr.appconfig.fakeAppConfigPersistenceManager
import nl.rijksoverheid.ctr.appconfig.fakeCachedAppConfigUseCase
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class AppConfigFreshnessUseCaseImplTest {

    @Test
    fun `config almost expired`() {
        val clock = Clock.fixed(Instant.parse("2022-04-25T09:00:00.00Z"), ZoneId.of("UTC"))
        val appConfigFreshnessUseCase = AppConfigFreshnessUseCaseImpl(
            fakeAppConfigPersistenceManager(
                lastFetchedSeconds = TimeUnit.SECONDS.convert(
                    clock.millis(),
                    TimeUnit.MILLISECONDS
                ) - 8
            ),
            clock,
            fakeCachedAppConfigUseCase(
                appConfig = fakeAppConfig(
                    configTtlSeconds = 10,
                    holderConfigAlmostOutOfDateWarningSeconds = 5
                )
            )
        )
        assertTrue(appConfigFreshnessUseCase.shouldShowConfigFreshnessWarning())
    }

    @Test
    fun `config not expiring`() {
        val clock = Clock.fixed(Instant.parse("2022-04-25T09:00:00.00Z"), ZoneId.of("UTC"))
        val appConfigFreshnessUseCase = AppConfigFreshnessUseCaseImpl(
            fakeAppConfigPersistenceManager(
                lastFetchedSeconds = TimeUnit.SECONDS.convert(
                    clock.millis(),
                    TimeUnit.MILLISECONDS
                ) - 4
            ),
            clock,
            fakeCachedAppConfigUseCase(
                appConfig = fakeAppConfig(
                    configTtlSeconds = 10,
                    holderConfigAlmostOutOfDateWarningSeconds = 5
                )
            )
        )
        assertFalse(appConfigFreshnessUseCase.shouldShowConfigFreshnessWarning())
    }

    @Test
    fun `config starts expiring`() {
        val clock = Clock.fixed(Instant.parse("2022-04-25T09:00:00.00Z"), ZoneId.of("UTC"))
        val appConfigFreshnessUseCase = AppConfigFreshnessUseCaseImpl(
            fakeAppConfigPersistenceManager(
                lastFetchedSeconds = TimeUnit.SECONDS.convert(
                    clock.millis(),
                    TimeUnit.MILLISECONDS
                ) - 5
            ),
            clock,
            fakeCachedAppConfigUseCase(
                appConfig = fakeAppConfig(
                    configTtlSeconds = 10,
                    holderConfigAlmostOutOfDateWarningSeconds = 5
                )
            )
        )
        assertTrue(appConfigFreshnessUseCase.shouldShowConfigFreshnessWarning())
    }
}