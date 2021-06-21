package nl.rijksoverheid.ctr.introduction.ui.status.usecases

import nl.rijksoverheid.ctr.introduction.IntroductionData
import nl.rijksoverheid.ctr.introduction.persistance.IntroductionPersistenceManager
import nl.rijksoverheid.ctr.introduction.ui.status.models.IntroductionStatus

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
        return if (introductionPersistenceManager.getIntroductionFinished()) {
            if (introductionData.newFeatures.isNotEmpty() &&
                !introductionPersistenceManager.getNewFeaturesSeen(introductionData.newFeatureVersion)
            ) {
                return IntroductionStatus.IntroductionFinished.NewFeatures(introductionData)
            }

            if (introductionData.newTerms != null && !introductionPersistenceManager.getNewTermsSeen(
                    introductionData.newTerms.version
                )
            ) {
                IntroductionStatus.IntroductionFinished.ConsentNeeded(introductionData)
            } else {
                IntroductionStatus.IntroductionFinished.NoActionRequired
            }
        } else {
            IntroductionStatus.IntroductionNotFinished(introductionData)
        }
    }

}
