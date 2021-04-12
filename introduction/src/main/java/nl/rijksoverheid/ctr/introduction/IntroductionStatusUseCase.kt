package nl.rijksoverheid.ctr.introduction

import nl.rijksoverheid.ctr.introduction.models.IntroductionStatus
import nl.rijksoverheid.ctr.introduction.models.NewTerms
import nl.rijksoverheid.ctr.introduction.persistance.IntroductionPersistenceManager

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface IntroductionStatusUseCase {
    fun get(newTerms: NewTerms? = null): IntroductionStatus
}

class IntroductionStatusUseCaseImpl(
    private val introductionPersistenceManager: IntroductionPersistenceManager
) : IntroductionStatusUseCase {
    override fun get(newTerms: NewTerms?): IntroductionStatus {
        val introductionFinished: Boolean = introductionPersistenceManager.getIntroductionFinished()

        return if (introductionFinished) {
            if (newTerms != null && !introductionPersistenceManager.getNewTermsSeen(newTerms.version)) {
                IntroductionStatus.IntroductionFinished.ConsentNeeded(
                    newTerms
                )
            } else {
                IntroductionStatus.IntroductionFinished.NoActionRequired
            }
        } else {
            IntroductionStatus.IntroductionNotFinished
        }
    }

}
