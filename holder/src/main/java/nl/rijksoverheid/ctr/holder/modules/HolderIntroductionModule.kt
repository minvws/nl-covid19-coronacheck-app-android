package nl.rijksoverheid.ctr.holder.modules

import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.usecases.HolderIntroductionStatusUseCaseImpl
import nl.rijksoverheid.ctr.introduction.privacy_consent.models.PrivacyPolicyItem
import nl.rijksoverheid.ctr.introduction.status.models.IntroductionData
import nl.rijksoverheid.ctr.introduction.status.usecases.IntroductionStatusUseCase
import org.koin.dsl.module

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
val holderIntroductionModule = module {
    factory {
        IntroductionData(
            onboardingItems = listOf(),
            privacyPolicyItems = listOf(
                PrivacyPolicyItem(
                    R.drawable.ic_shield,
                    R.string.privacy_policy_1
                ),
                PrivacyPolicyItem(
                    R.drawable.ic_shield,
                    R.string.privacy_policy_2
                )
            ),
            hideConsent = true
        )
    }
    factory<IntroductionStatusUseCase> {
        HolderIntroductionStatusUseCaseImpl(
            get(), get(), get(), get()
        )
    }
}
