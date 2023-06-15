package nl.rijksoverheid.ctr.holder.usecases

import com.squareup.moshi.Moshi
import java.net.UnknownHostException
import java.time.Clock
import java.time.OffsetDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.api.model.HolderConfig
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.appconfig.models.AppUpdateData
import nl.rijksoverheid.ctr.appconfig.models.ConfigResult
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigPersistenceManager
import nl.rijksoverheid.ctr.appconfig.persistence.AppUpdatePersistenceManager
import nl.rijksoverheid.ctr.appconfig.persistence.RecommendedUpdatePersistenceManager
import nl.rijksoverheid.ctr.appconfig.usecases.AppStatusUseCase
import nl.rijksoverheid.ctr.introduction.persistance.IntroductionPersistenceManager
import nl.rijksoverheid.ctr.persistence.HolderCachedAppConfigUseCase
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.shared.ext.toObject
import nl.rijksoverheid.ctr.shared.factories.ErrorCodeStringFactory
import nl.rijksoverheid.ctr.shared.factories.OnboardingFlow

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

class HolderAppStatusUseCaseImpl(
    private val clock: Clock,
    private val cachedAppConfigUseCase: HolderCachedAppConfigUseCase,
    private val appConfigPersistenceManager: AppConfigPersistenceManager,
    private val recommendedUpdatePersistenceManager: RecommendedUpdatePersistenceManager,
    private val moshi: Moshi,
    private val appUpdateData: AppUpdateData,
    private val appUpdatePersistenceManager: AppUpdatePersistenceManager,
    private val introductionPersistenceManager: IntroductionPersistenceManager,
    private val featureFlagUseCase: HolderFeatureFlagUseCase,
    private val holderDatabase: HolderDatabase,
    private val errorCodeStringFactory: ErrorCodeStringFactory
) : AppStatusUseCase {

    override suspend fun get(config: ConfigResult, currentVersionCode: Int): AppStatus =
        withContext(Dispatchers.IO) {
            when (config) {
                is ConfigResult.Success -> {
                    if (isArchived()) {
                        AppStatus.Archived
                    } else {
                        checkIfActionRequired(
                            currentVersionCode = currentVersionCode,
                            appConfig = config.appConfig.toObject<HolderConfig>(moshi)
                        )
                    }

                }
                is ConfigResult.Error -> {
                    val cachedAppConfig = cachedAppConfigUseCase.getCachedAppConfigOrNull()
                    when {
                        cachedAppConfig != null && appConfigPersistenceManager.getAppConfigLastFetchedSeconds() + cachedAppConfig.configTtlSeconds
                                >= OffsetDateTime.now(clock).toEpochSecond() -> {
                            checkIfActionRequired(
                                currentVersionCode = currentVersionCode,
                                appConfig = cachedAppConfig
                            )
                        }
                        config.error.e is UnknownHostException -> {
                            AppStatus.Error
                        }
                        else -> {
                            AppStatus.LaunchError(
                                errorCodeStringFactory.get(
                                    OnboardingFlow,
                                    listOf(config.error)
                                )
                            )
                        }
                    }
                }
            }
        }

    private fun updateRequired(currentVersionCode: Int, appConfig: AppConfig) =
        currentVersionCode < appConfig.minimumVersion

    override fun checkIfActionRequired(currentVersionCode: Int, appConfig: AppConfig): AppStatus {
        return when {
            updateRequired(currentVersionCode, appConfig) -> AppStatus.UpdateRequired
            appConfig.appDeactivated -> AppStatus.Deactivated
            shouldShowNewFeatures() -> getNewFeatures()
            newTermsAvailable() -> AppStatus.ConsentNeeded(appUpdateData)
            currentVersionCode < appConfig.recommendedVersion -> getHolderRecommendUpdateStatus(
                appConfig
            )
            else -> AppStatus.NoActionRequired
        }
    }

    private suspend fun isArchived(): Boolean {
        return featureFlagUseCase.isInArchiveMode() && holderDatabase.eventGroupDao().getAll().isEmpty()
    }

    private fun shouldShowNewFeatures() =
        (newFeaturesAvailable()) && introductionPersistenceManager.getIntroductionFinished()

    private fun getHolderRecommendUpdateStatus(appConfig: AppConfig) =
        if (appConfig.recommendedVersion > recommendedUpdatePersistenceManager.getHolderVersionUpdateShown()) {
            recommendedUpdatePersistenceManager.saveHolderVersionShown(appConfig.recommendedVersion)
            AppStatus.UpdateRecommended
        } else {
            AppStatus.NoActionRequired
        }

    override fun isAppActive(currentVersionCode: Int): Boolean {
        val config = cachedAppConfigUseCase.getCachedAppConfig()
        return !config.appDeactivated && !updateRequired(currentVersionCode, config)
    }

    private fun newFeaturesAvailable(): Boolean {
        val newFeatureVersion = appUpdateData.newFeatureVersion
        return appUpdateData.newFeatures.isNotEmpty() &&
                newFeatureVersion != null &&
                !appUpdatePersistenceManager.getNewFeaturesSeen(newFeatureVersion)
    }

    /**
     * Get the new feature and/or new policy rules as new feature based on policy change
     *
     * @return New features and/or policy change as new features
     */
    private fun getNewFeatures(): AppStatus.NewFeatures {
        return when {
            newFeaturesAvailable() -> AppStatus.NewFeatures(
                appUpdateData.copy(
                    newFeatures = appUpdateData.newFeatures
                )
            )
            else -> AppStatus.NewFeatures(appUpdateData)
        }
    }

    private fun newTermsAvailable() =
        !appUpdatePersistenceManager.getNewTermsSeen(appUpdateData.newTerms.version) &&
                introductionPersistenceManager.getIntroductionFinished()
}
