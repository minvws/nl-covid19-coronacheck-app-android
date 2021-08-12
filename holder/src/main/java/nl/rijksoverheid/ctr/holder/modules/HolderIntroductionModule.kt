package nl.rijksoverheid.ctr.holder.modules

import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.introduction.IntroductionData
import nl.rijksoverheid.ctr.introduction.ui.new_features.models.NewFeatureItem
import nl.rijksoverheid.ctr.introduction.ui.new_terms.models.NewTerms
import nl.rijksoverheid.ctr.introduction.ui.onboarding.models.OnboardingItem
import nl.rijksoverheid.ctr.introduction.ui.privacy_consent.models.PrivacyPolicyItem
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
            onboardingItems = listOf(
                OnboardingItem(
                    R.drawable.illustration_onboarding_1,
                    R.string.onboarding_screen_1_title,
                    R.string.onboarding_screen_1_description
                ),
                OnboardingItem(
                    R.drawable.illustration_onboarding_2,
                    R.string.onboarding_screen_2_title,
                    R.string.onboarding_screen_2_description,
                ),
                OnboardingItem(
                    R.drawable.illustration_onboarding_3,
                    R.string.onboarding_screen_4_title,
                    R.string.onboarding_screen_4_description,
                ),
                OnboardingItem(
                    R.drawable.illustration_onboarding_4,
                    R.string.onboarding_screen_3_title,
                    R.string.onboarding_screen_3_description,
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
                )
            ),
            newTerms = NewTerms(
                version = 2,
                needsConsent = false
            ),
            newFeatures = listOf(
                NewFeatureItem(
                    R.drawable.tabs,
                    R.string.new_in_app_tabs_title,
                    R.string.new_in_app_tabs_description,
                    R.color.primary_blue,
                    R.color.light_blue,
                )
            ),
            newFeatureVersion = 2,
            hideConsent = true
        )
    }
}
