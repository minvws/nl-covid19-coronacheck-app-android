package nl.rijksoverheid.ctr.holder.usecases

import androidx.annotation.StringRes
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.api.model.HolderConfig
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.appconfig.models.AppUpdateData
import nl.rijksoverheid.ctr.appconfig.models.ConfigResult
import nl.rijksoverheid.ctr.appconfig.models.NewFeatureItem
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigPersistenceManager
import nl.rijksoverheid.ctr.appconfig.persistence.AppUpdatePersistenceManager
import nl.rijksoverheid.ctr.appconfig.persistence.RecommendedUpdatePersistenceManager
import nl.rijksoverheid.ctr.appconfig.usecases.AppStatusUseCase
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.introduction.persistance.IntroductionPersistenceManager
import nl.rijksoverheid.ctr.persistence.HolderCachedAppConfigUseCase
import nl.rijksoverheid.ctr.persistence.PersistenceManager
import nl.rijksoverheid.ctr.shared.ext.toObject
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy
import java.time.Clock
import java.time.OffsetDateTime

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
    private val showNewDisclosurePolicyUseCase: ShowNewDisclosurePolicyUseCase,
    private val appUpdateData: AppUpdateData,
    private val persistenceManager: PersistenceManager,
    private val appUpdatePersistenceManager: AppUpdatePersistenceManager,
    private val introductionPersistenceManager: IntroductionPersistenceManager
) : AppStatusUseCase {

    override suspend fun get(config: ConfigResult, currentVersionCode: Int): AppStatus =
        withContext(Dispatchers.IO) {
            when (config) {
                is ConfigResult.Success -> {
                    checkIfActionRequired(
                        currentVersionCode = currentVersionCode,
                        appConfig = config.appConfig.toObject<HolderConfig>(moshi)
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

    private fun updateRequired(currentVersionCode: Int, appConfig: AppConfig) =
        currentVersionCode < appConfig.minimumVersion

    override fun checkIfActionRequired(currentVersionCode: Int, appConfig: AppConfig): AppStatus {
        val newPolicy = showNewDisclosurePolicyUseCase.get()
        return when {
            updateRequired(currentVersionCode, appConfig) -> AppStatus.UpdateRequired
            appConfig.appDeactivated -> AppStatus.Deactivated
            shouldShowNewFeatures(newPolicy) -> getNewFeatures(newPolicy)
            newTermsAvailable() -> AppStatus.ConsentNeeded(appUpdateData)
            currentVersionCode < appConfig.recommendedVersion -> getHolderRecommendUpdateStatus(
                appConfig
            )
            else -> AppStatus.NoActionRequired
        }
    }

    private fun shouldShowNewFeatures(newPolicy: DisclosurePolicy?) =
        (newFeaturesAvailable() || newPolicy != null) && introductionPersistenceManager.getIntroductionFinished()

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
     * @param[newPolicy] New policy or null if policy is not changed
     * @return New features and/or policy change as new features
     */
    private fun getNewFeatures(newPolicy: DisclosurePolicy?): AppStatus.NewFeatures {
        return when {
            newFeaturesAvailable() && newPolicy != null -> AppStatus.NewFeatures(
                appUpdateData.copy(
                    newFeatures = appUpdateData.newFeatures + listOf(
                        getNewPolicyFeatureItem(newPolicy)
                    ),
                ).apply {
                    setSavePolicyChange { persistenceManager.setPolicyScreenSeen(newPolicy) }
                })
            !newFeaturesAvailable() && newPolicy != null -> AppStatus.NewFeatures(
                appUpdateData.copy(
                    newFeatures = listOf(getNewPolicyFeatureItem(newPolicy))
                ).apply {
                    setSavePolicyChange { persistenceManager.setPolicyScreenSeen(newPolicy) }
                })
            else -> AppStatus.NewFeatures(appUpdateData)
        }
    }

    private fun newTermsAvailable() =
        !appUpdatePersistenceManager.getNewTermsSeen(appUpdateData.newTerms.version) &&
                introductionPersistenceManager.getIntroductionFinished()

    private fun getNewPolicyFeatureItem(newPolicy: DisclosurePolicy): NewFeatureItem {
        return NewFeatureItem(
            imageResource = R.drawable.illustration_new_disclosure_policy,
            titleResource = getPolicyFeatureTitle(newPolicy),
            description = getPolicyFeatureBody(newPolicy),
            subTitleColor = R.color.primary_blue,
            subtitleResource = getNewPolicySubtitle(newPolicy)
        )
    }

    @StringRes
    private fun getPolicyFeatureTitle(newPolicy: DisclosurePolicy): Int {
        return when (newPolicy) {
            DisclosurePolicy.ZeroG -> R.string.holder_newintheapp_content_onlyInternationalCertificates_0G_title
            DisclosurePolicy.OneG -> R.string.holder_newintheapp_content_only1G_title
            DisclosurePolicy.ThreeG -> R.string.holder_newintheapp_content_only3G_title
            DisclosurePolicy.OneAndThreeG -> R.string.holder_newintheapp_content_3Gand1G_title
        }
    }

    @StringRes
    private fun getPolicyFeatureBody(newPolicy: DisclosurePolicy): Int {
        return when (newPolicy) {
            DisclosurePolicy.ZeroG -> R.string.holder_newintheapp_content_onlyInternationalCertificates_0G_body
            DisclosurePolicy.OneG -> R.string.holder_newintheapp_content_only1G_body
            DisclosurePolicy.ThreeG -> R.string.holder_newintheapp_content_only3G_body
            DisclosurePolicy.OneAndThreeG -> R.string.holder_newintheapp_content_3Gand1G_body
        }
    }

    private fun getNewPolicySubtitle(newPolicy: DisclosurePolicy) =
        if (newPolicy == DisclosurePolicy.OneG || newPolicy == DisclosurePolicy.ThreeG) {
            R.string.general_newpolicy
        } else {
            R.string.new_in_app_subtitle
        }
}
