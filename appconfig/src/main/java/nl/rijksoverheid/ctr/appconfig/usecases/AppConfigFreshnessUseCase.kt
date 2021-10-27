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
    fun getAppConfigMaxValidityTimestamp() : Long
    fun shouldShowConfigFreshnessWarning() : Boolean
}

class AppConfigFreshnessUseCaseImpl(val appConfigPersistenceManager: AppConfigPersistenceManager, val clock : Clock, val cachedAppConfigUseCase: CachedAppConfigUseCase) : AppConfigFreshnessUseCase {
    override fun getAppConfigLastFetchedSeconds(): Long {
        return appConfigPersistenceManager.getAppConfigLastFetchedSeconds()
    }

    override fun getAppConfigMaxValidityTimestamp(): Long {
        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(getAppConfigLastFetchedSeconds()), ZoneOffset.UTC).plusDays(28).toEpochSecond()
    }

    override fun shouldShowConfigFreshnessWarning(): Boolean {
        // return true if config is older than 10 days && less than 28 days
        val lastFetched = getAppConfigLastFetchedSeconds()
        return lastFetched < OffsetDateTime.now(clock).minusDays(10).toEpochSecond()  && lastFetched > OffsetDateTime.now(clock).minusDays(28).toEpochSecond()
    }

}