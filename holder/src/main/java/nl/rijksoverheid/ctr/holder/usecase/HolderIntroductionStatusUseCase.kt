/*
 *  Copyright (c) 2022 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC    LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.usecase

import androidx.annotation.StringRes
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.ui.myoverview.usecases.ShowNewDisclosurePolicyUseCase
import nl.rijksoverheid.ctr.introduction.IntroductionData
import nl.rijksoverheid.ctr.introduction.persistance.IntroductionPersistenceManager
import nl.rijksoverheid.ctr.introduction.ui.new_features.models.NewFeatureItem
import nl.rijksoverheid.ctr.introduction.ui.onboarding.models.OnboardingItem
import nl.rijksoverheid.ctr.introduction.ui.status.models.IntroductionStatus
import nl.rijksoverheid.ctr.introduction.ui.status.models.IntroductionStatus.OnboardingFinished
import nl.rijksoverheid.ctr.introduction.ui.status.models.IntroductionStatus.OnboardingNotFinished
import nl.rijksoverheid.ctr.introduction.ui.status.usecases.IntroductionStatusUseCase
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy

class HolderIntroductionStatusUseCaseImpl(
    private val introductionPersistenceManager: IntroductionPersistenceManager,
    private val introductionData: IntroductionData,
    private val showNewDisclosurePolicyUseCase: ShowNewDisclosurePolicyUseCase,
    private val persistenceManager: PersistenceManager,
    private val holderFeatureFlagUseCase: HolderFeatureFlagUseCase
) : IntroductionStatusUseCase {

    override fun get(): IntroductionStatus {
        val newPolicy = showNewDisclosurePolicyUseCase.get()
        return when {
            introductionIsNotFinished() -> getIntroductionNotFinished()
            newFeaturesAvailable() || newPolicy != null -> getNewFeatures(newPolicy)
            newTermsAvailable() -> OnboardingFinished.ConsentNeeded(introductionData)
            else -> IntroductionStatus.IntroductionFinished
        }
    }

    private fun getIntroductionNotFinished(): OnboardingNotFinished {
        val policy = holderFeatureFlagUseCase.getDisclosurePolicy()
        return OnboardingNotFinished(
            introductionData.copy(
                onboardingItems = introductionData.onboardingItems + listOf(
                    OnboardingItem(
                        imageResource = R.drawable.illustration_new_disclosure_policy,
                        titleResource = getPolicyTitle(policy),
                        description = getPolicyBody(policy),
                        position = introductionData.onboardingItems.size + 1
                    )
                ),
                savePolicyChange = { persistenceManager.setPolicyScreenSeen(policy) }
            )
        )
    }

    /**
     * Get the new feature and/or new policy rules as new feature based on policy change
     *
     * @param[newPolicy] New policy or null if policy is not changed
     * @return New features and/or policy change as new features
     */
    private fun getNewFeatures(newPolicy: DisclosurePolicy?): OnboardingFinished.NewFeatures {
        return when {
            newFeaturesAvailable() && newPolicy != null -> OnboardingFinished.NewFeatures(
                introductionData.copy(
                    newFeatures = introductionData.newFeatures + listOf(getNewPolicyIntroduction(newPolicy)),
                    savePolicyChange = { persistenceManager.setPolicyScreenSeen(newPolicy) }
                )
            )
            !newFeaturesAvailable() && newPolicy != null -> OnboardingFinished.NewFeatures(
                introductionData.copy(
                    newFeatures = listOf(getNewPolicyIntroduction(newPolicy)),
                    newFeatureVersion = null,
                    savePolicyChange = { persistenceManager.setPolicyScreenSeen(newPolicy) }
                )
            )
            else -> OnboardingFinished.NewFeatures(introductionData)
        }
    }

    private fun getNewPolicyIntroduction(newPolicy: DisclosurePolicy): NewFeatureItem {
        return NewFeatureItem(
            imageResource = R.drawable.illustration_new_disclosure_policy,
            titleResource = getPolicyTitle(newPolicy),
            description = getPolicyBody(newPolicy),
            subTitleColor = R.color.primary_blue
        )
    }

    @StringRes
    private fun getPolicyTitle(newPolicy: DisclosurePolicy): Int {
        return when (newPolicy) {
            DisclosurePolicy.OneG -> R.string.holder_newintheapp_content_only1G_title
            DisclosurePolicy.ThreeG -> R.string.holder_newintheapp_content_only3G_title
            DisclosurePolicy.OneAndThreeG -> R.string.holder_newintheapp_content_3Gand1G_title
        }
    }

    @StringRes
    private fun getPolicyBody(newPolicy: DisclosurePolicy): Int {
        return when (newPolicy) {
            DisclosurePolicy.OneG -> R.string.holder_newintheapp_content_only1G_body
            DisclosurePolicy.ThreeG -> R.string.holder_newintheapp_content_only3G_body
            DisclosurePolicy.OneAndThreeG -> R.string.holder_newintheapp_content_3Gand1G_body
        }
    }

    private fun newTermsAvailable() =
        !introductionPersistenceManager.getNewTermsSeen(introductionData.newTerms.version)

    private fun newFeaturesAvailable(): Boolean {
        val newFeatureVersion = introductionData.newFeatureVersion
        return introductionData.newFeatures.isNotEmpty() &&
                newFeatureVersion != null &&
                !introductionPersistenceManager.getNewFeaturesSeen(newFeatureVersion)
    }

    private fun introductionIsNotFinished() =
        !introductionPersistenceManager.getIntroductionFinished()

}
