package nl.rijksoverheid.ctr.introduction

import androidx.lifecycle.ViewModel
import nl.rijksoverheid.ctr.introduction.persistance.IntroductionPersistenceManager
import nl.rijksoverheid.ctr.introduction.ui.new_terms.models.NewTerms
import nl.rijksoverheid.ctr.introduction.ui.status.models.IntroductionStatus
import nl.rijksoverheid.ctr.introduction.ui.status.usecases.IntroductionStatusUseCase

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

abstract class IntroductionViewModel : ViewModel() {
    abstract fun getIntroductionStatus(): IntroductionStatus
    abstract fun saveIntroductionFinished(introductionData: IntroductionData)
    abstract fun saveNewFeaturesFinished(newFeaturesVersion: Int)
}

class IntroductionViewModelImpl(
    private val introductionPersistenceManager: IntroductionPersistenceManager,
    private val introductionStatusUseCase: IntroductionStatusUseCase
) :
    IntroductionViewModel() {

    override fun getIntroductionStatus() = introductionStatusUseCase.get()

    override fun saveIntroductionFinished(introductionData: IntroductionData) {
        introductionPersistenceManager.saveIntroductionFinished()
        introductionData.newTerms?.let {
            introductionPersistenceManager.saveNewTermsSeen(it.version)
        }
        introductionPersistenceManager.saveNewFeaturesSeen(introductionData.newFeatureVersion)
    }

    override fun saveNewFeaturesFinished(newFeaturesVersion: Int) {
        introductionPersistenceManager.saveNewFeaturesSeen(newFeaturesVersion)
    }
}
