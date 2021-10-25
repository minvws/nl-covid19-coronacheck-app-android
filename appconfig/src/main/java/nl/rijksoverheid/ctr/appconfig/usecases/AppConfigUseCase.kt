/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig.usecases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigPersistenceManager
import nl.rijksoverheid.ctr.appconfig.models.ConfigResult
import nl.rijksoverheid.ctr.appconfig.repositories.ConfigRepository
import retrofit2.HttpException
import java.io.IOException
import java.time.Clock
import java.time.OffsetDateTime
import kotlin.math.abs

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

interface AppConfigUseCase {
    suspend fun get(): ConfigResult
    fun canRefresh(cachedAppConfigUseCase: CachedAppConfigUseCase): Boolean
}

class AppConfigUseCaseImpl(
    private val clock: Clock,
    private val appConfigPersistenceManager: AppConfigPersistenceManager,
    private val configRepository: ConfigRepository,
    private val clockDeviationUseCase: ClockDeviationUseCase
) : AppConfigUseCase {
    override suspend fun get(): ConfigResult = withContext(Dispatchers.IO) {
        try {
            val config = configRepository.getConfig()
            val success = ConfigResult.Success(
                appConfig = config.body,
                publicKeys = configRepository.getPublicKeys()
            )

            // the final server response timestamp is the timestamp of when the response firstly
            // generated plus the amount of seconds passed since then (date and age headers respectively)
            val serverDateMillis = config.headers.getDate("date")?.time ?: clock.millis()
            val serverAgeSeconds = config.headers["Age"]?.toInt() ?: 0
            val serverAgeMillis = serverAgeSeconds * 1000
            clockDeviationUseCase.store(
                serverResponseTimestamp = serverDateMillis + serverAgeMillis,
                localReceivedTimestamp = clock.millis()
            )

            appConfigPersistenceManager.saveAppConfigLastFetchedSeconds(
                OffsetDateTime.now(clock).toEpochSecond()
            )
            success
        } catch (e: IOException) {
            ConfigResult.Error
        } catch (e: HttpException) {
            ConfigResult.Error
        }
    }

    /**
     * never refresh more than once per configMinimumInterval (1 hour on prod)
     * in order not to track users by network requests
     */
    override fun canRefresh(cachedAppConfigUseCase: CachedAppConfigUseCase): Boolean {
        val lastTimeRefreshed = appConfigPersistenceManager.getAppConfigLastFetchedSeconds()
        val minimumRefreshInterval = cachedAppConfigUseCase.getCachedAppConfig().configMinimumIntervalSeconds
        val nowSeconds = OffsetDateTime.now(clock).toEpochSecond()

        // Check absolute value to handle clock deviations
        return abs(nowSeconds - lastTimeRefreshed) > minimumRefreshInterval
    }
}
