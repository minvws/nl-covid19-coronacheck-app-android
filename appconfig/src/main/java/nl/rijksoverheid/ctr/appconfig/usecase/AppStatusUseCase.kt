package nl.rijksoverheid.ctr.appconfig.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.appconfig.AppConfigPersistenceManager
import nl.rijksoverheid.ctr.appconfig.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.appconfig.model.AppStatus
import nl.rijksoverheid.ctr.appconfig.model.ConfigResult
import java.time.Clock
import java.time.OffsetDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface AppStatusUseCase {
    suspend fun get(config: ConfigResult, currentVersionCode: Int): AppStatus
}

class AppStatusUseCaseImpl(
    private val clock: Clock,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase,
    private val appConfigPersistenceManager: AppConfigPersistenceManager
) :
    AppStatusUseCase {
    override suspend fun get(config: ConfigResult, currentVersionCode: Int): AppStatus =
        withContext(Dispatchers.IO) {
            when (config) {
                is ConfigResult.Success -> {
                    val appConfig = config.appConfig
                    when {
                        appConfig.appDeactivated -> AppStatus.Deactivated(appConfig.informationURL)
                        currentVersionCode < appConfig.minimumVersion -> AppStatus.UpdateRequired
                        else -> AppStatus.NoActionRequired
                    }
                }
                is ConfigResult.Error -> {
                    val cachedAppConfig = cachedAppConfigUseCase.getCachedAppConfig()
                    if (cachedAppConfig == null) {
                        AppStatus.InternetRequired
                    } else {
                        if (appConfigPersistenceManager.getAppConfigLastFetchedSeconds() + cachedAppConfig.configTtlSeconds >= OffsetDateTime.now(
                                clock
                            )
                                .toEpochSecond()
                        ) {
                            AppStatus.NoActionRequired
                        } else {
                            AppStatus.InternetRequired
                        }
                    }
                }
            }
        }
}
