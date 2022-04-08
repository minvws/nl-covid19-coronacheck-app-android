/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig.usecases

import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigPersistenceManager
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

interface AppConfigFreshnessUseCase {
    fun getAppConfigLastFetchedSeconds(): Long
    fun getAppConfigMaxValidityTimestamp(): Long
    fun shouldShowConfigFreshnessWarning(): Boolean
}

class AppConfigFreshnessUseCaseImpl(
    val appConfigPersistenceManager: AppConfigPersistenceManager,
    val clock: Clock,
    val cachedAppConfigUseCase: CachedAppConfigUseCase
) : AppConfigFreshnessUseCase {
    override fun getAppConfigLastFetchedSeconds(): Long {
        return appConfigPersistenceManager.getAppConfigLastFetchedSeconds()
    }

    override fun getAppConfigMaxValidityTimestamp(): Long {
        return OffsetDateTime.ofInstant(
            Instant.ofEpochSecond(getAppConfigLastFetchedSeconds()),
            ZoneOffset.UTC
        ).plusSeconds(cachedAppConfigUseCase.getCachedAppConfig().configTtlSeconds.toLong())
            .toEpochSecond()
    }

    override fun shouldShowConfigFreshnessWarning(): Boolean {
        val lastFetched = getAppConfigLastFetchedSeconds()
        val now = OffsetDateTime.now(clock)
        val config = cachedAppConfigUseCase.getCachedAppConfig()
        val configNotExpiredYet =
            lastFetched > now.minusSeconds(config.configTtlSeconds.toLong()).toEpochSecond()
        val configAlmostExpired = lastFetched <= now.minusSeconds(config.configTtlSeconds.toLong())
            .plusSeconds(config.configAlmostOutOfDateWarningSeconds.toLong()).toEpochSecond()
        return lastFetched > 0 && configNotExpiredYet && configAlmostExpired

    }

}