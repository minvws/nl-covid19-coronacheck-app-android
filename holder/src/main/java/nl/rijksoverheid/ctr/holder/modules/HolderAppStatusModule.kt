package nl.rijksoverheid.ctr.holder.modules

import nl.rijksoverheid.ctr.appconfig.models.AppUpdateData
import nl.rijksoverheid.ctr.appconfig.models.NewFeatureItem
import nl.rijksoverheid.ctr.appconfig.models.NewTerms
import nl.rijksoverheid.ctr.appconfig.usecases.AppStatusUseCase
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.usecases.HolderAppStatusUseCaseImpl
import nl.rijksoverheid.ctr.holder.usecases.HolderIntroductionStatusUseCaseImpl
import nl.rijksoverheid.ctr.introduction.status.models.IntroductionData
import nl.rijksoverheid.ctr.introduction.privacy_consent.models.PrivacyPolicyItem
import nl.rijksoverheid.ctr.introduction.status.usecases.IntroductionStatusUseCase
import org.koin.dsl.module

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
val holderAppStatusModule = module {
    factory {
        AppUpdateData(
            newFeatures = listOf(),
            newTerms = NewTerms(
                version = 2,
                needsConsent = false
            ),
            newFeatureVersion = 2,
            hideConsent = true
        )
    }
    factory<AppStatusUseCase> {
        HolderAppStatusUseCaseImpl(
            get(), get(), get(), get(), get(), get(), get(), get(), get(), get()
        )
    }
}
