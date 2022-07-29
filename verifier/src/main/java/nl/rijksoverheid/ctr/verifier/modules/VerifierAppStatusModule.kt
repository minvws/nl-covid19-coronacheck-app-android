package nl.rijksoverheid.ctr.verifier.modules

import nl.rijksoverheid.ctr.appconfig.models.AppUpdateData
import nl.rijksoverheid.ctr.appconfig.models.NewFeatureItem
import nl.rijksoverheid.ctr.appconfig.models.NewTerms
import nl.rijksoverheid.ctr.appconfig.usecases.AppStatusUseCase
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.usecases.VerifierAppStatusUseCaseImpl
import org.koin.dsl.module

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
val verifierAppStatusModule = module {
    factory {
        AppUpdateData(
            newTerms = NewTerms(
                version = 1,
                needsConsent = true
            ),
            newFeatures = listOf(
                NewFeatureItem(
                    R.drawable.common_full_open_on_phone,
                    R.string.new_in_app_risksetting_title,
                    R.string.new_in_app_risksetting_subtitle,
                    R.color.primary_blue,
                    R.color.light_blue,
                    lastButtonResource = R.string.onboarding_next
                )
            ),
            newFeatureVersion = 1
        )
    }
    factory<AppStatusUseCase> {
        VerifierAppStatusUseCaseImpl(
            get(), get(), get(), get(), get(), get(), get(), get(), get()
        )
    }
}
