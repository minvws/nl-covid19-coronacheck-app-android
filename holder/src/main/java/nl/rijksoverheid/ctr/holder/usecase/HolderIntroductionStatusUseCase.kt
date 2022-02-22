/*
 *  Copyright (c) 2022 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC    LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.usecase

import nl.rijksoverheid.ctr.appconfig.usecases.FeatureFlagUseCase
import nl.rijksoverheid.ctr.introduction.IntroductionData
import nl.rijksoverheid.ctr.introduction.persistance.IntroductionPersistenceManager
import nl.rijksoverheid.ctr.introduction.ui.status.models.IntroductionStatus
import nl.rijksoverheid.ctr.introduction.ui.status.models.IntroductionStatus.IntroductionFinished
import nl.rijksoverheid.ctr.introduction.ui.status.models.IntroductionStatus.IntroductionNotFinished
import nl.rijksoverheid.ctr.introduction.ui.status.usecases.IntroductionStatusUseCase

class HolderIntroductionStatusUseCaseImpl(
    private val introductionPersistenceManager: IntroductionPersistenceManager,
    private val introductionData: IntroductionData,
    private val featureFlagUseCase: FeatureFlagUseCase
) : IntroductionStatusUseCase {

    override fun get(): IntroductionStatus {
        return when {
            introductionIsNotFinished() -> IntroductionNotFinished(introductionData)
            newFeaturesAvailable() -> IntroductionFinished.NewFeatures(introductionData)
            newTermsAvailable() -> IntroductionFinished.ConsentNeeded(introductionData)
            else -> IntroductionFinished.NoActionRequired
        }
    }

    private fun newTermsAvailable() =
                !introductionPersistenceManager.getNewTermsSeen(introductionData.newTerms.version)

    private fun newFeaturesAvailable() = introductionData.newFeatures.isNotEmpty() &&
            !introductionPersistenceManager.getNewFeaturesSeen(introductionData.newFeatureVersion) && featureFlagUseCase.isVerificationPolicySelectionEnabled()

    private fun introductionIsNotFinished() =
        !introductionPersistenceManager.getIntroductionFinished()

}
