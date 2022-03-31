package nl.rijksoverheid.ctr.verifier.usecases

import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.api.model.VerifierConfig
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.appconfig.models.AppUpdateData
import nl.rijksoverheid.ctr.appconfig.models.ConfigResult
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigPersistenceManager
import nl.rijksoverheid.ctr.appconfig.persistence.AppUpdatePersistenceManager
import nl.rijksoverheid.ctr.appconfig.persistence.RecommendedUpdatePersistenceManager
import nl.rijksoverheid.ctr.appconfig.usecases.AppStatusUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.FeatureFlagUseCase
import nl.rijksoverheid.ctr.introduction.persistance.IntroductionPersistenceManager
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

class VerifierAppStatusUseCaseImpl(
    private val clock: Clock,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase,
    private val appConfigPersistenceManager: AppConfigPersistenceManager,
    private val recommendedUpdatePersistenceManager: RecommendedUpdatePersistenceManager,
    private val moshi: Moshi,
    private val appUpdateData: AppUpdateData,
    private val appUpdatePersistenceManager: AppUpdatePersistenceManager,
    private val introductionPersistenceManager: IntroductionPersistenceManager,
    private val featureFlagUseCase: FeatureFlagUseCase
    ) : AppStatusUseCase {

    override suspend fun get(config: ConfigResult, currentVersionCode: Int): AppStatus =
        withContext(Dispatchers.IO) {
            when (config) {
                is ConfigResult.Success -> {
                    checkIfActionRequired(
                        currentVersionCode = currentVersionCode,
                        appConfig = config.appConfig.toObject<VerifierConfig>(moshi)
                    )
                }
                is ConfigResult.Error -> {
                    val cachedAppConfig = cachedAppConfigUseCase.getCachedAppConfigOrNull()
                    if (cachedAppConfig != null && appConfigPersistenceManager.getAppConfigLastFetchedSeconds() + cachedAppConfig.configTtlSeconds
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

    private fun updateRequired(currentVersionCode: Int, appConfig: AppConfig) = currentVersionCode < appConfig.minimumVersion

    override fun checkIfActionRequired(currentVersionCode: Int, appConfig: AppConfig): AppStatus {
        return when {
            updateRequired(currentVersionCode, appConfig) -> AppStatus.UpdateRequired
            appConfig.appDeactivated -> AppStatus.Deactivated
            newFeaturesAvailable() -> AppStatus.NewFeatures(appUpdateData)
            newTermsAvailable() -> AppStatus.ConsentNeeded(appUpdateData)
            currentVersionCode < appConfig.recommendedVersion -> getVerifierRecommendedUpdateStatus(appConfig)
            else -> AppStatus.NoActionRequired
        }
    }

    private fun newFeaturesAvailable(): Boolean {
        val newFeatureVersion = appUpdateData.newFeatureVersion
        return appUpdateData.newFeatures.isNotEmpty() &&
                newFeatureVersion != null &&
                !appUpdatePersistenceManager.getNewFeaturesSeen(newFeatureVersion) &&
                featureFlagUseCase.isVerificationPolicySelectionEnabled() &&
                introductionPersistenceManager.getIntroductionFinished()
    }

    private fun newTermsAvailable() =
        !appUpdatePersistenceManager.getNewTermsSeen(appUpdateData.newTerms.version)
                && introductionPersistenceManager.getIntroductionFinished()

    private fun getVerifierRecommendedUpdateStatus(appConfig: AppConfig): AppStatus {
        val localTime = TimeUnit.MILLISECONDS.toSeconds(clock.instant().toEpochMilli())
        val updateLastShown =
            recommendedUpdatePersistenceManager.getRecommendedUpdateShownSeconds()
        val updateIntervalSeconds =
            TimeUnit.HOURS.toSeconds(appConfig.recommendedUpgradeIntervalHours.toLong())
        return if (localTime > updateLastShown + updateIntervalSeconds) {
            recommendedUpdatePersistenceManager.saveRecommendedUpdateShownSeconds(localTime)
            AppStatus.UpdateRecommended
        } else {
            AppStatus.NoActionRequired
        }
    }

    override fun isAppActive(currentVersionCode: Int): Boolean {
        val config = cachedAppConfigUseCase.getCachedAppConfig()
        return !config.appDeactivated && !updateRequired(currentVersionCode, config)
    }
}
