package nl.rijksoverheid.ctr.appconfig.usecases

import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigPersistenceManager
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.appconfig.models.ConfigResult
import nl.rijksoverheid.ctr.shared.ext.toObject
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
    private val moshi: Moshi,
) :
    AppStatusUseCase {

    override suspend fun get(config: ConfigResult, currentVersionCode: Int): AppStatus =
        withContext(Dispatchers.IO) {
            when (config) {
                is ConfigResult.Success -> {
                    checkIfActionRequired(
                        currentVersionCode = currentVersionCode,
                        appConfig = config.appConfig.toObject(moshi)
                    )
                }
                is ConfigResult.Error -> {
                    val cachedAppConfig = cachedAppConfigUseCase.getCachedAppConfig()
                    if (cachedAppConfig == null) {
                        AppStatus.Error
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
                            AppStatus.Error
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
