package nl.rijksoverheid.ctr.holder.modules

import nl.rijksoverheid.ctr.holder.HolderMainActivityViewModel
import nl.rijksoverheid.ctr.holder.HolderMainActivityViewModelImpl
import nl.rijksoverheid.ctr.holder.ui.create_qr.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.digid.DigiDFragment
import nl.rijksoverheid.ctr.holder.ui.create_qr.digid.LoginViewModel
import nl.rijksoverheid.ctr.holder.ui.create_qr.paper_proof.PaperProofCodeViewModel
import nl.rijksoverheid.ctr.holder.ui.create_qr.paper_proof.PaperProofCodeViewModelImpl
import nl.rijksoverheid.ctr.holder.ui.create_qr.paper_proof.PaperProofQrScannerViewModel
import nl.rijksoverheid.ctr.holder.ui.create_qr.paper_proof.PaperProofQrScannerViewModelImpl
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.DigidAuthenticationRepository
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.MijnCNAuthenticationRepository
import nl.rijksoverheid.ctr.holder.ui.device_rooted.DeviceRootedViewModel
import nl.rijksoverheid.ctr.holder.ui.device_rooted.DeviceRootedViewModelImpl
import nl.rijksoverheid.ctr.holder.ui.device_secure.DeviceSecureViewModel
import nl.rijksoverheid.ctr.holder.ui.device_secure.DeviceSecureViewModelImpl
import nl.rijksoverheid.ctr.holder.ui.myoverview.DashboardViewModel
import nl.rijksoverheid.ctr.holder.ui.myoverview.DashboardViewModelImpl
import nl.rijksoverheid.ctr.holder.ui.myoverview.QrCodesViewModel
import nl.rijksoverheid.ctr.holder.ui.myoverview.QrCodesViewModelImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.core.qualifier.qualifier
import org.koin.dsl.module

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
val viewModels = module {
    viewModel<QrCodesViewModel> { QrCodesViewModelImpl(get(), get()) }
    viewModel<HolderMainActivityViewModel> { HolderMainActivityViewModelImpl() }
    viewModel(named("digid")) {
        LoginViewModel(get(named("digid")), get())
    }
    viewModel(named("mijncn")) {
        LoginViewModel(get(named("mijncn")), get())
    }
    viewModel<InputTokenViewModel> { InputTokenViewModelImpl(get(), get()) }
    viewModel { TokenQrViewModel(get()) }
    viewModel<DeviceRootedViewModel> { DeviceRootedViewModelImpl(get(), get()) }
    viewModel<DeviceSecureViewModel> { DeviceSecureViewModelImpl(get(), get()) }
    viewModel<YourEventsViewModel> { YourEventsViewModelImpl(get(), get(), get(), get()) }
    viewModel<GetEventsViewModel> { GetEventsViewModelImpl(get(), get()) }
    viewModel<PaperProofCodeViewModel> { PaperProofCodeViewModelImpl(get()) }
    viewModel<PaperProofQrScannerViewModel> { PaperProofQrScannerViewModelImpl(get()) }
    viewModel<DashboardViewModel> { DashboardViewModelImpl(get(), get(), get(), get(), get(), get()) }
    viewModel<SyncGreenCardsViewModel> { SyncGreenCardsViewModelImpl(get(), get()) }
}

enum class LoginViewModelQualifier{
    DIGID, MIJN_CN
}
