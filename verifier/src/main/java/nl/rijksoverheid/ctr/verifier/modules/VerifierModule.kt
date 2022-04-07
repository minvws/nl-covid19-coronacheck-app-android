/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.verifier.modules

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import nl.rijksoverheid.ctr.appconfig.usecases.ReturnToExternalAppUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.ReturnToExternalAppUseCaseImpl
import nl.rijksoverheid.ctr.shared.BuildConfigUseCase
import nl.rijksoverheid.ctr.verifier.managers.DeeplinkManager
import nl.rijksoverheid.ctr.verifier.managers.DeeplinkManagerImpl
import nl.rijksoverheid.ctr.verifier.VerifierMainActivityViewModel
import nl.rijksoverheid.ctr.verifier.VerifierMainActivityViewModelImpl
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
import nl.rijksoverheid.ctr.verifier.persistance.SharedPreferencesPersistenceManager
import nl.rijksoverheid.ctr.verifier.persistance.usecase.RandomKeyUseCase
import nl.rijksoverheid.ctr.verifier.persistance.usecase.RandomKeyUseCaseImpl
import nl.rijksoverheid.ctr.verifier.persistance.usecase.VerifierCachedAppConfigUseCase
import nl.rijksoverheid.ctr.verifier.persistance.usecase.VerifierCachedAppConfigUseCaseImpl
import nl.rijksoverheid.ctr.verifier.instructions.ScanInstructionsButtonUtil
import nl.rijksoverheid.ctr.verifier.instructions.ScanInstructionsButtonUtilImpl
import nl.rijksoverheid.ctr.verifier.policy.*
import nl.rijksoverheid.ctr.verifier.scanlog.ScanLogViewModel
import nl.rijksoverheid.ctr.verifier.scanlog.ScanLogViewModelImpl
import nl.rijksoverheid.ctr.verifier.scanlog.datamapper.ScanLogDataMapper
import nl.rijksoverheid.ctr.verifier.scanlog.datamapper.ScanLogDataMapperImpl
import nl.rijksoverheid.ctr.verifier.scanlog.items.util.ScanLogFirstInstallTimeAdapterItemUtil
import nl.rijksoverheid.ctr.verifier.scanlog.items.util.ScanLogFirstInstallTimeAdapterItemUtilImpl
import nl.rijksoverheid.ctr.verifier.scanlog.items.util.ScanLogListAdapterItemUtil
import nl.rijksoverheid.ctr.verifier.scanlog.items.util.ScanLogListAdapterItemUtilImpl
import nl.rijksoverheid.ctr.verifier.scanlog.repositories.ScanLogRepository
import nl.rijksoverheid.ctr.verifier.scanlog.repositories.ScanLogRepositoryImpl
import nl.rijksoverheid.ctr.verifier.scanlog.usecase.*
import nl.rijksoverheid.ctr.verifier.scanner.ScannerViewModel
import nl.rijksoverheid.ctr.verifier.scanner.ScannerViewModelImpl
import nl.rijksoverheid.ctr.verifier.scanner.usecases.TestResultValidUseCase
import nl.rijksoverheid.ctr.verifier.scanner.usecases.TestResultValidUseCaseImpl
import nl.rijksoverheid.ctr.verifier.scanner.usecases.VerifyQrUseCase
import nl.rijksoverheid.ctr.verifier.scanner.usecases.VerifyQrUseCaseImpl
import nl.rijksoverheid.ctr.verifier.scanner.utils.ScannerUtil
import nl.rijksoverheid.ctr.verifier.scanner.utils.ScannerUtilImpl
import nl.rijksoverheid.ctr.verifier.scanqr.*
import nl.rijksoverheid.ctr.verifier.scanqr.util.MenuUtil
import nl.rijksoverheid.ctr.verifier.scanqr.util.MenuUtilImpl
import nl.rijksoverheid.ctr.verifier.scanqr.util.ScannerStateCountdownUtil
import nl.rijksoverheid.ctr.verifier.scanqr.util.ScannerStateCountdownUtilImpl
import nl.rijksoverheid.ctr.verifier.usecases.*
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import java.time.Clock

/**
 * Configure app config dependencies
 */
fun verifierModule() = module {

    single<PersistenceManager> {
        SharedPreferencesPersistenceManager(
            get(), get()
        )
    }

    // Use cases
    factory<VerifyQrUseCase> {
        VerifyQrUseCaseImpl(get(), get())
    }
    factory<TestResultValidUseCase> {
        TestResultValidUseCaseImpl(get())
    }
    factory<ReturnToExternalAppUseCase> { ReturnToExternalAppUseCaseImpl(get()) }
    factory<RandomKeyUseCase> { RandomKeyUseCaseImpl(get(), get()) }
    factory<GetScanLogItemsUseCase> { GetScanLogItemsUseCaseImpl(get(), get(), get()) }
    factory<VerifierCachedAppConfigUseCase> {
        VerifierCachedAppConfigUseCaseImpl(
            get()
        )
    }
    factory<ScanLogsCleanupUseCase> { ScanLogsCleanupUseCaseImpl(Clock.systemUTC(), get(), get()) }
    factory<LogScanUseCase> { LogScanUseCaseImpl(Clock.systemUTC(), get(), get()) }
    factory<ScannerUsedRecentlyUseCase> { ScannerUsedRecentlyUseCaseImpl(get(), get(), get()) }
    factory<VerifierFeatureFlagUseCase> { VerifierFeatureFlagUseCaseImpl(get()) }

    // Utils
    factory<ScannerUtil> { ScannerUtilImpl() }
    factory<ScanLogListAdapterItemUtil> { ScanLogListAdapterItemUtilImpl() }
    factory<ScanLogFirstInstallTimeAdapterItemUtil> { ScanLogFirstInstallTimeAdapterItemUtilImpl(Clock.systemUTC()) }
    factory<ScanInstructionsButtonUtil> { ScanInstructionsButtonUtilImpl(get()) }
    factory<MenuUtil> { MenuUtilImpl(get(), get(), get()) }

    // ViewModels
    viewModel<VerifierMainActivityViewModel> { VerifierMainActivityViewModelImpl(get(), get(), get()) }
    viewModel<ScanQrViewModel> { ScanQrViewModelImpl(get(), get(), get(), get()) }
    viewModel<ScannerViewModel> { ScannerViewModelImpl(get(), get(), get()) }
    viewModel<ScanLogViewModel> { ScanLogViewModelImpl(get()) }
    viewModel<NewPolicyRulesViewModel> { NewPolicyRulesViewModelImpl(get(), get(), get()) }

    // Repositories
    factory<ScanLogRepository> { ScanLogRepositoryImpl(get(), get()) }

    // Data mappers
    factory<ScanLogDataMapper> { ScanLogDataMapperImpl() }

    single {
        get<Moshi.Builder>(Moshi.Builder::class)
            .add(KotlinJsonAdapterFactory()).build()
    }

    factory<VerificationPolicySelectionUseCase> { VerificationPolicySelectionUseCaseImpl(get(), get(), get()) }
    factory<VerificationPolicySelectionStateUseCase> { VerificationPolicySelectionStateUseCaseImpl(get(), get()) }
    factory<ConfigVerificationPolicyUseCase> { ConfigVerificationPolicyUseCaseImpl(get(), get(), get()) }
    factory<ScannerNavigationStateUseCase> { ScannerNavigationStateUseCaseImpl(get(), get(), get()) }
    factory<ScannerStateUseCase> { ScannerStateUseCaseImpl(get(), get(), get(), get(), get()) }
    factory<ScannerStateCountdownUtil> { ScannerStateCountdownUtilImpl(get(), get(), get()) }
    factory<NewPolicyRulesItemUseCase> { NewPolicyRulesItemUseCaseImpl(get()) }

    viewModel<VerificationPolicySelectionViewModel> { VerificationPolicySelectionViewModelImpl(get(), get()) }

    factory<BuildConfigUseCase> {
        BuildConfigUseCaseImpl()
    }

    single<DeeplinkManager> { DeeplinkManagerImpl(get(), get(), get()) }
}
