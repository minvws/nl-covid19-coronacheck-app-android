package nl.rijksoverheid.ctr.introduction.setup

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

abstract class SetupViewModel : ViewModel() {
    val introductionDataLiveData: LiveData<IntroductionData> = MutableLiveData()
    abstract fun onConfigUpdated()
}

class SetupViewModelImpl(
    private val introductionStatusUseCase: IntroductionStatusUseCase
) : SetupViewModel() {

    override fun onConfigUpdated() {
        (introductionDataLiveData as MutableLiveData).postValue(introductionStatusUseCase.getData())
    }
}
