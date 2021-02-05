package nl.rijksoverheid.ctr.holder

import androidx.preference.PreferenceManager
import nl.rijksoverheid.ctr.holder.myqr.MyQrViewModel
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.persistence.SharedPreferencesPersistenceManager
import nl.rijksoverheid.ctr.holder.repositories.AuthenticationRepository
import nl.rijksoverheid.ctr.holder.repositories.HolderRepository
import nl.rijksoverheid.ctr.holder.status.StatusViewModel
import nl.rijksoverheid.ctr.holder.usecase.*
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
        )
    }
    single {
        SecretKeyUseCase(get())
    }
    single {
        CommitmentMessageUseCase(get())
    }
    single {
        OnboardingUseCase(get())
    }

    // ViewModels
    viewModel { StatusViewModel(get(), get()) }
    viewModel { MyQrViewModel(get(), get(), get()) }

    // Repositories
    single { AuthenticationRepository() }
    single { HolderRepository(get()) }
}
