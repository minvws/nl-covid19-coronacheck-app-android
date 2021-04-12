package nl.rijksoverheid.ctr.introduction

import androidx.lifecycle.ViewModel
import nl.rijksoverheid.ctr.introduction.models.IntroductionStatus
import nl.rijksoverheid.ctr.introduction.models.NewTerms
import nl.rijksoverheid.ctr.introduction.onboarding.models.OnboardingItem
import nl.rijksoverheid.ctr.introduction.persistance.IntroductionPersistenceManager
import nl.rijksoverheid.ctr.introduction.privacy_consent.models.PrivacyPolicyItem

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

abstract class IntroductionViewModel : ViewModel() {
    abstract fun getIntroductionStatus(
        onboardingItems: List<OnboardingItem>,
        privacyPolicyItems: List<PrivacyPolicyItem>,
        newTerms: NewTerms? = null
    ): IntroductionStatus

    abstract fun getIntroductionFinished(): Boolean
    abstract fun saveIntroductionFinished(newTerms: NewTerms? = null)
}

class IntroductionViewModelImpl(
    private val introductionPersistenceManager: IntroductionPersistenceManager,
    private val introductionStatusUseCase: IntroductionStatusUseCase
) :
    IntroductionViewModel() {

    override fun getIntroductionStatus(
        onboardingItems: List<OnboardingItem>,
        privacyPolicyItems: List<PrivacyPolicyItem>,
        newTerms: NewTerms?
    ) =
        introductionStatusUseCase.get(onboardingItems, privacyPolicyItems, newTerms)

    override fun getIntroductionFinished(): Boolean {
        return introductionPersistenceManager.getIntroductionFinished()
    }

    override fun saveIntroductionFinished(newTerms: NewTerms?) {
        introductionPersistenceManager.saveIntroductionFinished()
        newTerms?.let {
            introductionPersistenceManager.saveNewTermsSeen(it.version)
        }
    }
}
