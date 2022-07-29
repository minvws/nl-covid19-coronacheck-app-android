package nl.rijksoverheid.ctr.verifier.modules

import nl.rijksoverheid.ctr.introduction.onboarding.models.OnboardingItem
import nl.rijksoverheid.ctr.introduction.privacy_consent.models.PrivacyPolicyItem
import nl.rijksoverheid.ctr.introduction.status.models.IntroductionData
import nl.rijksoverheid.ctr.introduction.status.usecases.IntroductionStatusUseCase
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.usecases.VerifierIntroductionStatusUseCaseImpl
import org.koin.dsl.module

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
val verifierIntroductionModule = module {
    factory {
        IntroductionData(
            onboardingItems = listOf(
                OnboardingItem(
                    R.drawable.illustration_onboarding_1,
                    R.string.onboarding_screen_1_title,
                    R.string.onboarding_screen_1_description
                )
            ),
            privacyPolicyItems = listOf(
                PrivacyPolicyItem(
                    R.drawable.shield,
                    R.string.privacy_policy_1
                ),
                PrivacyPolicyItem(
                    R.drawable.shield,
                    R.string.privacy_policy_2
                ),
                PrivacyPolicyItem(
                    R.drawable.shield,
                    R.string.privacy_policy_3
                )
            )
        )
    }
    factory<IntroductionStatusUseCase> { VerifierIntroductionStatusUseCaseImpl(get(), get()) }
}
