package nl.rijksoverheid.ctr.appconfig.usecases

import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.api.model.HolderConfig
import nl.rijksoverheid.ctr.appconfig.api.model.VerifierConfig
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.appconfig.models.ConfigResult
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigPersistenceManager
import nl.rijksoverheid.ctr.appconfig.persistence.RecommendedUpdatePersistenceManager
import nl.rijksoverheid.ctr.shared.ext.toObject
import java.time.Clock
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface AppStatusUseCase {
    suspend fun get(config: ConfigResult, currentVersionCode: Int): AppStatus
    fun setUpdateRecommendedIsHandled()
}

class AppStatusUseCaseImpl(
    private val clock: Clock,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase,
    private val appConfigPersistenceManager: AppConfigPersistenceManager,
    private val recommendedUpdatePersistenceManager: RecommendedUpdatePersistenceManager,
    private val moshi: Moshi,
    private val isVerifierApp: Boolean,
) :
    AppStatusUseCase {

    override suspend fun get(config: ConfigResult, currentVersionCode: Int): AppStatus =
        withContext(Dispatchers.IO) {
            when (config) {
                is ConfigResult.Success -> {
                    checkIfActionRequired(
                        currentVersionCode = currentVersionCode,
                        appConfig = if (isVerifierApp) {
                            config.appConfig.toObject<VerifierConfig>(moshi)
                        } else {
                            config.appConfig.toObject<HolderConfig>(moshi)
                        }
                    )
                }
                is ConfigResult.Error -> {
                    val cachedAppConfig = cachedAppConfigUseCase.getCachedAppConfig()
                    if (appConfigPersistenceManager.getAppConfigLastFetchedSeconds() + cachedAppConfig.configTtlSeconds
                        >= OffsetDateTime.now(clock).toEpochSecond()
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

    override fun setUpdateRecommendedIsHandled() {
        recommendedUpdatePersistenceManager.saveRecommendedUpdateShownSeconds(getLocalTime())
    }

    private fun checkIfActionRequired(currentVersionCode: Int, appConfig: AppConfig): AppStatus {
        return when {
            appConfig.appDeactivated -> AppStatus.Deactivated(appConfig.informationURL)
            currentVersionCode < appConfig.minimumVersion -> AppStatus.UpdateRequired
            currentVersionCode < appConfig.recommendedVersion -> getUpdateRecommendedStatus(appConfig)
            else -> AppStatus.NoActionRequired
        }
    }

    private fun getUpdateRecommendedStatus(appConfig: AppConfig): AppStatus {
        val updateLastShown = recommendedUpdatePersistenceManager.getRecommendedUpdateShownSeconds()
        val updateIntervalSeconds =
            TimeUnit.HOURS.toSeconds(appConfig.recommendedUpgradeIntervalHours.toLong())


        return if (getLocalTime() > updateLastShown + updateIntervalSeconds) {
            AppStatus.UpdateRecommended
        } else {
            AppStatus.NoActionRequired
        }
    }

    private fun getLocalTime() = TimeUnit.MILLISECONDS.toSeconds(clock.instant().toEpochMilli())
}
