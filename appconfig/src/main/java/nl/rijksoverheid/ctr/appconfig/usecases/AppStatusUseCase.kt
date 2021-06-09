package nl.rijksoverheid.ctr.appconfig.usecases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigPersistenceManager
import nl.rijksoverheid.ctr.appconfig.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.appconfig.models.ConfigResult
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
    private val appConfigPersistenceManager: AppConfigPersistenceManager,
) :
    AppStatusUseCase {

    override suspend fun get(config: ConfigResult, currentVersionCode: Int): AppStatus =
        withContext(Dispatchers.IO) {
            when (config) {
                is ConfigResult.Success -> {
                    checkIfActionRequired(
                        currentVersionCode = currentVersionCode,
                        appConfig = config.appConfig
                    )
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
                            checkIfActionRequired(
                                currentVersionCode = currentVersionCode,
                                appConfig = cachedAppConfig
                            )
                        } else {
                            AppStatus.InternetRequired
                        }
                    }
                }
            }
        }

    private fun checkIfActionRequired(currentVersionCode: Int, appConfig: AppConfig): AppStatus {
        return when {
            appConfig.appDeactivated -> AppStatus.Deactivated(appConfig.informationURL)
            currentVersionCode < appConfig.minimumVersion -> AppStatus.UpdateRequired
            else -> AppStatus.NoActionRequired
        }
    }
}
