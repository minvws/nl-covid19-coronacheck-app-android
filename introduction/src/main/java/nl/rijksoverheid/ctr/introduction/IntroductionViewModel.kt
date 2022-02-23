package nl.rijksoverheid.ctr.introduction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.rijksoverheid.ctr.introduction.persistance.IntroductionPersistenceManager
import nl.rijksoverheid.ctr.introduction.ui.status.models.IntroductionStatus
import nl.rijksoverheid.ctr.introduction.ui.status.models.IntroductionStatus.IntroductionFinished
import nl.rijksoverheid.ctr.introduction.ui.status.models.IntroductionStatus.IntroductionNotFinished
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
    val setupRequiredLiveData: LiveData<Event<Unit>> = MutableLiveData()
    val introductionNotFinishedLiveData: LiveData<Event<IntroductionNotFinished>> = MutableLiveData()
    val introductionFinishedLiveData: LiveData<Event<IntroductionFinished>> = MutableLiveData()
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
        val introductionStatus = introductionStatusUseCase.get()
        if (introductionStatus is IntroductionNotFinished) {
            (setupRequiredLiveData as MutableLiveData).postValue(Event(Unit))
        }
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
    }

    override fun saveNewFeaturesFinished(newFeaturesVersion: Int) {
        introductionPersistenceManager.saveNewFeaturesSeen(newFeaturesVersion)
    }

    override fun onConfigUpdated() {
        when (val introductionStatus = introductionStatusUseCase.get()) {
            is IntroductionNotFinished -> {
                (introductionNotFinishedLiveData as MutableLiveData)
                    .postValue(Event(introductionStatus))
            }
            is IntroductionFinished -> {
                (introductionFinishedLiveData as MutableLiveData)
                    .postValue(Event(introductionStatus))
            }
        }
    }
}
