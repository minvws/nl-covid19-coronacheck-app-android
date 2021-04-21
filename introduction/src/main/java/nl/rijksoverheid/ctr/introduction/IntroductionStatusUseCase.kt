package nl.rijksoverheid.ctr.introduction

import nl.rijksoverheid.ctr.introduction.models.IntroductionData
import nl.rijksoverheid.ctr.introduction.models.IntroductionStatus
import nl.rijksoverheid.ctr.introduction.persistance.IntroductionPersistenceManager

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
        val introductionFinished: Boolean = introductionPersistenceManager.getIntroductionFinished()

        return if (introductionFinished) {
            if (introductionData.newTerms != null && !introductionPersistenceManager.getNewTermsSeen(
                    introductionData.newTerms.version
                )
            ) {
                IntroductionStatus.IntroductionFinished.ConsentNeeded(
                    introductionData.newTerms
                )
            } else {
                IntroductionStatus.IntroductionFinished.NoActionRequired
            }
        } else {
            IntroductionStatus.IntroductionNotFinished(
                introductionData
            )
        }
    }

}
