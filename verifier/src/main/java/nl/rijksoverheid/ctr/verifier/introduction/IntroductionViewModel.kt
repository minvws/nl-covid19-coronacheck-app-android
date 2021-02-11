package nl.rijksoverheid.ctr.verifier.introduction

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.rijksoverheid.ctr.holder.usecase.IntroductionUseCase
import nl.rijksoverheid.ctr.verifier.status.models.IntroductionState

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class IntroductionViewModel(private val introductionUseCase: IntroductionUseCase) : ViewModel() {

    val introductionStateLiveData = MutableLiveData<IntroductionState>()

    fun getIntroductionState() {
        introductionStateLiveData.postValue(
            IntroductionState(
                onboardingFinished = introductionUseCase.onboardingFinished(),
                privacyPolicyFinished = introductionUseCase.privacyPolicyFinished()
            )
        )
    }

    fun setOnboardingFinished() = introductionUseCase.setOnboardingFinished()
    fun setPrivacyPolicyFinished() = introductionUseCase.setPrivacyPolicyFinished()

}
