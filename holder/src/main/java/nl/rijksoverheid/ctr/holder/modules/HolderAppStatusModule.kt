package nl.rijksoverheid.ctr.holder.modules

import nl.rijksoverheid.ctr.appconfig.models.AppUpdateData
import nl.rijksoverheid.ctr.appconfig.models.NewFeatureItem
import nl.rijksoverheid.ctr.appconfig.models.NewTerms
import nl.rijksoverheid.ctr.appconfig.usecases.AppStatusUseCase
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.usecases.HolderAppStatusUseCaseImpl
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
            newFeatures = listOf(
                NewFeatureItem(
                    imageResource = R.drawable.ic_paper_proof_international_qr,
                    subTitleColor = R.color.link,
                    titleResource = R.string.holder_newintheapp_foreignproofs_title,
                    description = R.string.holder_newintheapp_foreignproofs_body
                )
            ),
            newTerms = NewTerms(
                version = 2,
                needsConsent = false
            ),
            newFeatureVersion = 3,
            hideConsent = true
        )
    }
    factory<AppStatusUseCase> {
        HolderAppStatusUseCaseImpl(
            get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()
        )
    }
}
