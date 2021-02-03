package nl.rijksoverheid.ctr.holder

import androidx.preference.PreferenceManager
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.persistence.SharedPreferencesPersistenceManager
import nl.rijksoverheid.ctr.holder.repositories.AuthenticationRepository
import nl.rijksoverheid.ctr.holder.repositories.HolderRepository
import nl.rijksoverheid.ctr.holder.usecase.CommitmentMessageUseCase
import nl.rijksoverheid.ctr.holder.usecase.GenerateHolderQrCodeUseCase
import nl.rijksoverheid.ctr.holder.usecase.HolderQrCodeUseCase
import nl.rijksoverheid.ctr.holder.usecase.SecretKeyUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
val mainModule = module {

    single<PersistenceManager> {
        SharedPreferencesPersistenceManager(
            PreferenceManager.getDefaultSharedPreferences(
                androidContext()
            )
        )
    }

    // Use cases
    single {
        GenerateHolderQrCodeUseCase(
            get(),
            get(),
            get()
        )
    }
    single {
        HolderQrCodeUseCase(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }
    single {
        SecretKeyUseCase(get())
    }
    single {
        CommitmentMessageUseCase(get())
    }

    // ViewModels
    viewModel { HolderViewModel(get(), get()) }

    // Repositories
    single { AuthenticationRepository() }
    single { HolderRepository(get()) }
}
