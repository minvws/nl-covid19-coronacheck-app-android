package nl.rijksoverheid.ctr.introduction.ui.status.usecases

import nl.rijksoverheid.ctr.introduction.IntroductionData
import nl.rijksoverheid.ctr.introduction.persistance.IntroductionPersistenceManager
import nl.rijksoverheid.ctr.introduction.ui.status.models.IntroductionStatus
import nl.rijksoverheid.ctr.introduction.ui.status.models.IntroductionStatus.IntroductionFinished
import nl.rijksoverheid.ctr.introduction.ui.status.models.IntroductionStatus.IntroductionNotFinished

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC    LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface IntroductionStatusUseCase {
    fun get(): IntroductionStatus
}

class IntroductionStatusUseCaseImpl(
    private val introductionPersistenceManager: IntroductionPersistenceManager,
    private val introductionData: IntroductionData
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
        introductionData.newTerms != null &&
                !introductionPersistenceManager.getNewTermsSeen(introductionData.newTerms.version)

    private fun newFeaturesAvailable() = introductionData.newFeatures.isNotEmpty() &&
            !introductionPersistenceManager.getNewFeaturesSeen(introductionData.newFeatureVersion)

    private fun introductionIsNotFinished() =
        !introductionPersistenceManager.getIntroductionFinished()

}
