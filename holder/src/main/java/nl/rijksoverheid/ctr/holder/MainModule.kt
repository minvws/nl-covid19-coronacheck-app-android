package nl.rijksoverheid.ctr.holder

import androidx.preference.PreferenceManager
import nl.rijksoverheid.ctr.holder.digid.DigiDViewModel
import nl.rijksoverheid.ctr.holder.introduction.IntroductionViewModel
import nl.rijksoverheid.ctr.holder.myoverview.LocalTestResultViewModel
import nl.rijksoverheid.ctr.holder.myoverview.QrCodeViewModel
import nl.rijksoverheid.ctr.holder.myoverview.TestResultsViewModel
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.persistence.SharedPreferencesPersistenceManager
import nl.rijksoverheid.ctr.holder.repositories.AuthenticationRepository
import nl.rijksoverheid.ctr.holder.repositories.HolderRepository
import nl.rijksoverheid.ctr.holder.usecase.CommitmentMessageUseCase
import nl.rijksoverheid.ctr.holder.usecase.GenerateHolderQrCodeUseCase
import nl.rijksoverheid.ctr.holder.usecase.IntroductionUseCase
import nl.rijksoverheid.ctr.holder.usecase.LocalTestResultUseCase
import nl.rijksoverheid.ctr.holder.usecase.QrCodeUseCase
import nl.rijksoverheid.ctr.holder.usecase.SecretKeyUseCase
import nl.rijksoverheid.ctr.holder.usecase.TestProviderUseCase
import nl.rijksoverheid.ctr.holder.usecase.TestResultAttributesUseCase
import nl.rijksoverheid.ctr.holder.usecase.TestResultUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
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
                androidContext(),
            ), get()
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
        QrCodeUseCase(
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
        IntroductionUseCase(get())
    }
    single {
        TestProviderUseCase(get())
    }
    single {
        TestResultUseCase(get(), get(), get(), get())
    }
    single {
        LocalTestResultUseCase(get(), get(), get(), get())
    }
    single {
        TestResultAttributesUseCase(get())
    }

    // ViewModels
    viewModel { IntroductionViewModel(get()) }
    viewModel { QrCodeViewModel(get()) }
    viewModel { LocalTestResultViewModel(get(), get()) }
    viewModel { DigiDViewModel(get()) }
    viewModel { TestResultsViewModel(get(), get(), get()) }

    // Repositories
    single { AuthenticationRepository() }
    single {
        HolderRepository(
            get(),
            get(),
            get(named("SignedResponseWithModel")),
            get(named("ResponseError"))
        )
    }
}
