package nl.rijksoverheid.ctr.shared

import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.holder.util.EventUtil
import nl.rijksoverheid.ctr.shared.api.TestApiClient
import nl.rijksoverheid.ctr.shared.repositories.EventRepository
import nl.rijksoverheid.ctr.shared.usecases.SignatureValidUseCase
import nl.rijksoverheid.ctr.shared.util.CryptoUtil
import nl.rijksoverheid.ctr.shared.util.QrCodeUtils
import nl.rijksoverheid.ctr.shared.util.ZxingQrCodeUtils
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
val sharedModule = module {

    single {
        val retroFit = Retrofit.Builder()
            .baseUrl("https://api-ct.bananenhalen.nl")
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
        retroFit.create(TestApiClient::class.java)
    }
    single { Moshi.Builder().build() }
    single<QrCodeUtils> { ZxingQrCodeUtils() }

    // Utils
    single { EventUtil() }
    single { CryptoUtil() }

    // Use cases
    single {
        SignatureValidUseCase(
            get(),
            get()
        )
    }

    // Repositories
    single { EventRepository(get()) }
}
