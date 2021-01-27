package nl.rijksoverheid.ctr.verifier

import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.citizen.util.EventUtil
import nl.rijksoverheid.ctr.shared.api.TestApiClient
import nl.rijksoverheid.ctr.shared.repositories.EventRepository
import nl.rijksoverheid.ctr.shared.usecases.SignatureValidUseCase
import nl.rijksoverheid.ctr.shared.util.CryptoUtil
import nl.rijksoverheid.ctr.shared.util.QrCodeUtils
import nl.rijksoverheid.ctr.shared.util.ZxingQrCodeUtils
import nl.rijksoverheid.ctr.verifier.usecases.DecryptCitizenQrUseCase
import nl.rijksoverheid.ctr.verifier.usecases.VerifierAllowsCitizenUseCase
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
val verifierModule = module {
    single {
        DecryptCitizenQrUseCase(
            get(),
            get()
        )
    }
    single {
        VerifierAllowsCitizenUseCase(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    // ViewModels
    viewModel { VerifierViewModel(get()) }
}
