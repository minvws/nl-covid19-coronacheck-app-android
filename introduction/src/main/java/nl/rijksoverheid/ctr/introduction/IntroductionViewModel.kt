package nl.rijksoverheid.ctr.introduction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.rijksoverheid.ctr.introduction.persistance.IntroductionPersistenceManager
import nl.rijksoverheid.ctr.introduction.ui.status.models.IntroductionStatus
import nl.rijksoverheid.ctr.introduction.ui.status.usecases.IntroductionStatusUseCase
import nl.rijksoverheid.ctr.shared.livedata.Event

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

abstract class IntroductionViewModel : ViewModel() {
    val introductionStatusLiveData: LiveData<Event<IntroductionStatus>> = MutableLiveData()
    abstract fun getIntroductionStatus(): IntroductionStatus
    abstract fun saveIntroductionFinished(introductionData: IntroductionData)
    abstract fun saveNewFeaturesFinished(newFeaturesVersion: Int)
    abstract fun onConfigUpdated()
}

class IntroductionViewModelImpl(
    private val introductionPersistenceManager: IntroductionPersistenceManager,
    private val introductionStatusUseCase: IntroductionStatusUseCase
) : IntroductionViewModel() {

    init {
        postIntroductionStatus()
    }

    override fun getIntroductionStatus() = introductionStatusUseCase.get()

    override fun saveIntroductionFinished(introductionData: IntroductionData) {
        introductionPersistenceManager.saveIntroductionFinished()
        introductionPersistenceManager.saveNewTermsSeen(introductionData.newTerms.version)
        introductionData.newFeatureVersion?.let {
            introductionPersistenceManager.saveNewFeaturesSeen(
                it
            )
        }
        introductionData.savePolicyChange?.invoke()
    }

    override fun saveNewFeaturesFinished(newFeaturesVersion: Int) {
        introductionPersistenceManager.saveNewFeaturesSeen(newFeaturesVersion)
    }

    override fun onConfigUpdated() {
        introductionPersistenceManager.saveSetupFinished()
        postIntroductionStatus()
    }

    private fun postIntroductionStatus() {
        introductionStatusUseCase.get()
            .takeIf { it !is IntroductionStatus.IntroductionFinished }
            ?.let {
                (introductionStatusLiveData as MutableLiveData).postValue(Event(it))
            }
    }
}
