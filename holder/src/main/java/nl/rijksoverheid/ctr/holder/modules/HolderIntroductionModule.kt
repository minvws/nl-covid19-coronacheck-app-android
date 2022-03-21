package nl.rijksoverheid.ctr.holder.modules

import nl.rijksoverheid.ctr.appconfig.models.AppUpdateData
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.usecase.HolderIntroductionStatusUseCaseImpl
import nl.rijksoverheid.ctr.introduction.IntroductionData
import nl.rijksoverheid.ctr.appconfig.models.NewFeatureItem
import nl.rijksoverheid.ctr.appconfig.models.NewTerms
import nl.rijksoverheid.ctr.introduction.ui.privacy_consent.models.PrivacyPolicyItem
import nl.rijksoverheid.ctr.introduction.ui.status.usecases.IntroductionStatusUseCase
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
    factory {
        AppUpdateData(
            newFeatures = listOf(
                NewFeatureItem(
                    R.drawable.tabs,
                    R.string.new_in_app_tabs_title,
                    R.string.new_in_app_tabs_description,
                    R.color.primary_blue,
                    R.color.light_blue,
                )
            ),
            newTerms = NewTerms(
                version = 2,
                needsConsent = false
            ),
            newFeatureVersion = 2,
            hideConsent = true
        )
    }
    factory<IntroductionStatusUseCase> { HolderIntroductionStatusUseCaseImpl(get(), get(), get(), get(), get()) }
}
