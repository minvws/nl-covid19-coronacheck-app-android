package nl.rijksoverheid.ctr.introduction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.rijksoverheid.ctr.introduction.persistance.IntroductionPersistenceManager
import nl.rijksoverheid.ctr.introduction.status.models.IntroductionData
import nl.rijksoverheid.ctr.introduction.status.models.IntroductionStatus
import nl.rijksoverheid.ctr.introduction.status.usecases.IntroductionStatusUseCase
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
    abstract fun onConfigUpdated()
    abstract fun init()
}

class IntroductionViewModelImpl(
    private val introductionPersistenceManager: IntroductionPersistenceManager,
    private val introductionStatusUseCase: IntroductionStatusUseCase
) : IntroductionViewModel() {


    override fun init() {
        postIntroductionStatus()
    }

    override fun getIntroductionStatus() = introductionStatusUseCase.get()

    override fun saveIntroductionFinished(introductionData: IntroductionData) {
        introductionPersistenceManager.saveIntroductionFinished()
        introductionData.savePolicyChange()
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
