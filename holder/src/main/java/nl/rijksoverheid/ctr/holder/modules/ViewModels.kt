package nl.rijksoverheid.ctr.holder.modules

import nl.rijksoverheid.ctr.design.menu.MenuViewModel
import nl.rijksoverheid.ctr.holder.HolderMainActivityViewModel
import nl.rijksoverheid.ctr.holder.HolderMainActivityViewModelImpl
import nl.rijksoverheid.ctr.holder.dashboard.DashboardViewModel
import nl.rijksoverheid.ctr.holder.dashboard.DashboardViewModelImpl
import nl.rijksoverheid.ctr.holder.data_migration.DataMigrationScanQrViewModel
import nl.rijksoverheid.ctr.holder.data_migration.DataMigrationScanQrViewModelImpl
import nl.rijksoverheid.ctr.holder.data_migration.DataMigrationShowQrCodeViewModel
import nl.rijksoverheid.ctr.holder.data_migration.DataMigrationShowQrCodeViewModelImpl
import nl.rijksoverheid.ctr.holder.data_migration.DataMigrationStartViewModel
import nl.rijksoverheid.ctr.holder.data_migration.DataMigrationStartViewModelImpl
import nl.rijksoverheid.ctr.holder.get_events.GetEventsViewModel
import nl.rijksoverheid.ctr.holder.get_events.GetEventsViewModelImpl
import nl.rijksoverheid.ctr.holder.get_events.LoginViewModel
import nl.rijksoverheid.ctr.holder.input_token.InputTokenViewModel
import nl.rijksoverheid.ctr.holder.input_token.InputTokenViewModelImpl
import nl.rijksoverheid.ctr.holder.menu.MenuViewModelImpl
import nl.rijksoverheid.ctr.holder.modules.qualifier.LoginQualifier
import nl.rijksoverheid.ctr.holder.paper_proof.PaperProofDomesticInputCodeViewModel
import nl.rijksoverheid.ctr.holder.paper_proof.PaperProofDomesticInputCodeViewModelImpl
import nl.rijksoverheid.ctr.holder.paper_proof.PaperProofQrScannerViewModel
import nl.rijksoverheid.ctr.holder.paper_proof.PaperProofQrScannerViewModelImpl
import nl.rijksoverheid.ctr.holder.pdf.PdfWebViewModel
import nl.rijksoverheid.ctr.holder.pdf.PdfWebViewModelImpl
import nl.rijksoverheid.ctr.holder.qrcodes.QrCodesViewModel
import nl.rijksoverheid.ctr.holder.qrcodes.QrCodesViewModelImpl
import nl.rijksoverheid.ctr.holder.saved_events.SavedEventsViewModel
import nl.rijksoverheid.ctr.holder.sync_greencards.SyncGreenCardsViewModel
import nl.rijksoverheid.ctr.holder.sync_greencards.SyncGreenCardsViewModelImpl
import nl.rijksoverheid.ctr.holder.ui.device_rooted.DeviceRootedViewModel
import nl.rijksoverheid.ctr.holder.ui.device_rooted.DeviceRootedViewModelImpl
import nl.rijksoverheid.ctr.holder.ui.device_secure.DeviceSecureViewModel
import nl.rijksoverheid.ctr.holder.ui.device_secure.DeviceSecureViewModelImpl
import nl.rijksoverheid.ctr.holder.ui.priority_notification.PriorityNotificationViewModel
import nl.rijksoverheid.ctr.holder.ui.priority_notification.PriorityNotificationViewModelImpl
import nl.rijksoverheid.ctr.holder.your_events.YourEventsViewModel
import nl.rijksoverheid.ctr.holder.your_events.YourEventsViewModelImpl
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
val viewModels = module {
    viewModel<QrCodesViewModel> { QrCodesViewModelImpl(get(), get(), get()) }
    viewModel<HolderMainActivityViewModel> { HolderMainActivityViewModelImpl() }
    viewModel<InputTokenViewModel> { InputTokenViewModelImpl(get(), get()) }
    viewModel(named(LoginQualifier.DIGID)) {
        LoginViewModel(get(named(LoginQualifier.DIGID)), get())
    }
    viewModel(named(LoginQualifier.MIJN_CN)) {
        LoginViewModel(get(named(LoginQualifier.MIJN_CN)), get())
    }
    viewModel<DeviceRootedViewModel> { DeviceRootedViewModelImpl(get(), get()) }
    viewModel<DeviceSecureViewModel> { DeviceSecureViewModelImpl(get(), get()) }
    viewModel<YourEventsViewModel> { YourEventsViewModelImpl(get(), get(), get()) }
    viewModel<GetEventsViewModel> { GetEventsViewModelImpl(get(), get(), get()) }
    viewModel<PaperProofDomesticInputCodeViewModel> { PaperProofDomesticInputCodeViewModelImpl(get(), get()) }
    viewModel<PaperProofQrScannerViewModel> { PaperProofQrScannerViewModelImpl(get()) }
    viewModel<DashboardViewModel> { DashboardViewModelImpl(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel<SyncGreenCardsViewModel> { SyncGreenCardsViewModelImpl(get(), get()) }
    viewModel { SavedEventsViewModel(get(), get()) }
    viewModel<MenuViewModel> { MenuViewModelImpl(get(), get()) }
    viewModel<PriorityNotificationViewModel> { PriorityNotificationViewModelImpl(get()) }
    viewModel<DataMigrationStartViewModel> { DataMigrationStartViewModelImpl(get()) }
    viewModel<DataMigrationShowQrCodeViewModel> { DataMigrationShowQrCodeViewModelImpl(get(), get()) }
    viewModel<DataMigrationScanQrViewModel> { DataMigrationScanQrViewModelImpl(get(), get(), get()) }
    viewModel<PdfWebViewModel> { PdfWebViewModelImpl(androidContext().filesDir.path, get(), get()) }
}
