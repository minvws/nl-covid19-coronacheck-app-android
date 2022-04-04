package nl.rijksoverheid.ctr.introduction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.rijksoverheid.ctr.introduction.persistance.IntroductionPersistenceManager
import nl.rijksoverheid.ctr.introduction.status.models.IntroductionData
import nl.rijksoverheid.ctr.introduction.status.usecases.IntroductionStatusUseCase

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

abstract class IntroductionViewModel : ViewModel() {
    val introductionRequiredLiveData: LiveData<Unit> = MutableLiveData()
    abstract fun getIntroductionRequired(): Boolean
    abstract fun saveIntroductionFinished(introductionData: IntroductionData)
}

class IntroductionViewModelImpl(
    private val introductionPersistenceManager: IntroductionPersistenceManager,
    private val introductionStatusUseCase: IntroductionStatusUseCase
) : IntroductionViewModel() {

    init {
        if (introductionStatusUseCase.getIntroductionRequired()) {
            (introductionRequiredLiveData as MutableLiveData).postValue(Unit)
        }
    }

    override fun getIntroductionRequired() = introductionStatusUseCase.getIntroductionRequired()

    override fun saveIntroductionFinished(introductionData: IntroductionData) {
        introductionPersistenceManager.saveIntroductionFinished()
        introductionData.savePolicyChange()
    }
}
