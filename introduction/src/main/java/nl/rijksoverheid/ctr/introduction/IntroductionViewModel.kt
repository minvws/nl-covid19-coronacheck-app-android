package nl.rijksoverheid.ctr.introduction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.rijksoverheid.ctr.introduction.persistance.IntroductionPersistenceManager
import nl.rijksoverheid.ctr.introduction.ui.status.models.IntroductionStatus
import nl.rijksoverheid.ctr.introduction.ui.status.models.IntroductionStatus.OnboardingFinished
import nl.rijksoverheid.ctr.introduction.ui.status.models.IntroductionStatus.OnboardingNotFinished
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
    val onboardingNotFinishedLiveData: LiveData<Event<OnboardingNotFinished>> = MutableLiveData()
    val onboardingFinishedLiveData: LiveData<Event<OnboardingFinished>> = MutableLiveData()
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
        if (introductionStatus is OnboardingNotFinished) {
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
            is OnboardingNotFinished -> {
                (onboardingNotFinishedLiveData as MutableLiveData)
                    .postValue(Event(introductionStatus))
            }
            is OnboardingFinished -> {
                (onboardingFinishedLiveData as MutableLiveData)
                    .postValue(Event(introductionStatus))
            }
            IntroductionStatus.IntroductionFinished -> {

            }
        }
    }
}
