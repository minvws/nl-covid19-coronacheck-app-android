package nl.rijksoverheid.ctr.holder.modules

import java.time.Clock
import nl.rijksoverheid.ctr.holder.dashboard.items.DashboardGreenCardAdapterItemUtil
import nl.rijksoverheid.ctr.holder.dashboard.items.DashboardGreenCardAdapterItemUtilImpl
import nl.rijksoverheid.ctr.holder.dashboard.items.DashboardHeaderAdapterItemUtil
import nl.rijksoverheid.ctr.holder.dashboard.items.DashboardHeaderAdapterItemUtilImpl
import nl.rijksoverheid.ctr.holder.dashboard.items.DashboardInfoCardAdapterItemUtil
import nl.rijksoverheid.ctr.holder.dashboard.items.DashboardInfoCardAdapterItemUtilImpl
import nl.rijksoverheid.ctr.holder.dashboard.util.CardItemUtil
import nl.rijksoverheid.ctr.holder.dashboard.util.CardItemUtilImpl
import nl.rijksoverheid.ctr.holder.dashboard.util.CredentialUtil
import nl.rijksoverheid.ctr.holder.dashboard.util.CredentialUtilImpl
import nl.rijksoverheid.ctr.holder.dashboard.util.DashboardItemEmptyStateUtil
import nl.rijksoverheid.ctr.holder.dashboard.util.DashboardItemEmptyStateUtilImpl
import nl.rijksoverheid.ctr.holder.dashboard.util.DashboardItemUtil
import nl.rijksoverheid.ctr.holder.dashboard.util.DashboardItemUtilImpl
import nl.rijksoverheid.ctr.holder.dashboard.util.DashboardPageInfoItemHandlerUtil
import nl.rijksoverheid.ctr.holder.dashboard.util.DashboardPageInfoItemHandlerUtilImpl
import nl.rijksoverheid.ctr.holder.dashboard.util.OriginUtil
import nl.rijksoverheid.ctr.holder.dashboard.util.OriginUtilImpl
import nl.rijksoverheid.ctr.holder.dashboard.util.RemovedEventsBottomSheetUtil
import nl.rijksoverheid.ctr.holder.dashboard.util.RemovedEventsBottomSheetUtilImpl
import nl.rijksoverheid.ctr.holder.data_migration.StringDataZipper
import nl.rijksoverheid.ctr.holder.data_migration.StringDataZipperImpl
import nl.rijksoverheid.ctr.holder.get_events.utils.LoginTypeUtil
import nl.rijksoverheid.ctr.holder.get_events.utils.LoginTypeUtilImpl
import nl.rijksoverheid.ctr.holder.get_events.utils.ScopeUtil
import nl.rijksoverheid.ctr.holder.get_events.utils.ScopeUtilImpl
import nl.rijksoverheid.ctr.holder.menu.AboutThisAppDataModel
import nl.rijksoverheid.ctr.holder.menu.AboutThisAppDataModelImpl
import nl.rijksoverheid.ctr.holder.menu.HelpMenuDataModel
import nl.rijksoverheid.ctr.holder.menu.HelpMenuDataModelImpl
import nl.rijksoverheid.ctr.holder.no_digid.NoDigidScreenDataUtil
import nl.rijksoverheid.ctr.holder.no_digid.NoDigidScreenDataUtilImpl
import nl.rijksoverheid.ctr.holder.paper_proof.utils.PaperProofUtil
import nl.rijksoverheid.ctr.holder.paper_proof.utils.PaperProofUtilImpl
import nl.rijksoverheid.ctr.holder.qrcodes.models.ReadEuropeanCredentialUtil
import nl.rijksoverheid.ctr.holder.qrcodes.models.ReadEuropeanCredentialUtilImpl
import nl.rijksoverheid.ctr.holder.qrcodes.utils.MultipleQrCodesUtil
import nl.rijksoverheid.ctr.holder.qrcodes.utils.MultipleQrCodesUtilImpl
import nl.rijksoverheid.ctr.holder.qrcodes.utils.QrCodesFragmentUtil
import nl.rijksoverheid.ctr.holder.qrcodes.utils.QrCodesFragmentUtilImpl
import nl.rijksoverheid.ctr.holder.utils.CountryUtil
import nl.rijksoverheid.ctr.holder.utils.CountryUtilImpl
import nl.rijksoverheid.ctr.holder.utils.LocalDateUtil
import nl.rijksoverheid.ctr.holder.utils.LocalDateUtilImpl
import nl.rijksoverheid.ctr.holder.utils.StringUtil
import nl.rijksoverheid.ctr.holder.utils.StringUtilImpl
import nl.rijksoverheid.ctr.holder.your_events.utils.EventGroupEntityUtil
import nl.rijksoverheid.ctr.holder.your_events.utils.EventGroupEntityUtilImpl
import nl.rijksoverheid.ctr.holder.your_events.utils.RemoteEventHolderUtil
import nl.rijksoverheid.ctr.holder.your_events.utils.RemoteEventHolderUtilImpl
import nl.rijksoverheid.ctr.holder.your_events.utils.RemoteEventStringUtil
import nl.rijksoverheid.ctr.holder.your_events.utils.RemoteEventStringUtilImpl
import nl.rijksoverheid.ctr.holder.your_events.utils.RemoteEventUtil
import nl.rijksoverheid.ctr.holder.your_events.utils.RemoteEventUtilImpl
import nl.rijksoverheid.ctr.holder.your_events.utils.RemoteProtocol3Util
import nl.rijksoverheid.ctr.holder.your_events.utils.RemoteProtocol3UtilImpl
import nl.rijksoverheid.ctr.holder.your_events.utils.YourEventsEndStateUtil
import nl.rijksoverheid.ctr.holder.your_events.utils.YourEventsEndStateUtilImpl
import nl.rijksoverheid.ctr.holder.your_events.utils.YourEventsFragmentUtil
import nl.rijksoverheid.ctr.holder.your_events.utils.YourEventsFragmentUtilImpl
import nl.rijksoverheid.ctr.holder.your_events.widgets.YourEventWidgetUtil
import nl.rijksoverheid.ctr.holder.your_events.widgets.YourEventWidgetUtilImpl
import nl.rijksoverheid.rdo.modules.luhncheck.TokenValidator
import nl.rijksoverheid.rdo.modules.luhncheck.TokenValidatorImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
val utilsModule = module {
    factory<DashboardGreenCardAdapterItemUtil> {
        DashboardGreenCardAdapterItemUtilImpl(
            Clock.systemUTC(),
            androidContext(),
            get(),
            get()
        )
    }
    factory<TokenValidator> { TokenValidatorImpl() }
    factory<CredentialUtil> { CredentialUtilImpl(Clock.systemUTC(), get(), get(), get(), get()) }
    factory<OriginUtil> { OriginUtilImpl(Clock.systemUTC()) }
    factory<RemoteEventHolderUtil> { RemoteEventHolderUtilImpl(get(), get(), get(), get()) }
    factory<RemoteProtocol3Util> { RemoteProtocol3UtilImpl() }
    factory<RemoteEventUtil> { RemoteEventUtilImpl(get()) }
    factory<RemoteEventStringUtil> { RemoteEventStringUtilImpl(androidContext()::getString) }
    factory<ReadEuropeanCredentialUtil> { ReadEuropeanCredentialUtilImpl(get()) }
    factory<DashboardItemUtil> {
        DashboardItemUtilImpl(
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    factory<CountryUtil> { CountryUtilImpl() }
    factory<LocalDateUtil> { LocalDateUtilImpl(get(), get()) }
    factory<MultipleQrCodesUtil> { MultipleQrCodesUtilImpl() }
    factory<DashboardPageInfoItemHandlerUtil> {
        DashboardPageInfoItemHandlerUtilImpl(
            get(),
            get(),
            get()
        )
    }
    factory<QrCodesFragmentUtil> { QrCodesFragmentUtilImpl(Clock.systemUTC()) }
    factory<YourEventsFragmentUtil> { YourEventsFragmentUtilImpl(get()) }
    factory<YourEventWidgetUtil> { YourEventWidgetUtilImpl() }
    factory<DashboardInfoCardAdapterItemUtil> { DashboardInfoCardAdapterItemUtilImpl() }
    factory<DashboardItemEmptyStateUtil> { DashboardItemEmptyStateUtilImpl() }
    factory<AboutThisAppDataModel> { AboutThisAppDataModelImpl(get(), get()) }
    factory<HelpMenuDataModel> { HelpMenuDataModelImpl(get(), get(), get()) }
    factory<ScopeUtil> { ScopeUtilImpl() }
    factory<LoginTypeUtil> { LoginTypeUtilImpl() }
    factory<DashboardHeaderAdapterItemUtil> { DashboardHeaderAdapterItemUtilImpl() }
    factory<CardItemUtil> { CardItemUtilImpl() }
    factory<EventGroupEntityUtil> { EventGroupEntityUtilImpl(get()) }
    factory<PaperProofUtil> { PaperProofUtilImpl(get(), get(), get()) }
    factory<NoDigidScreenDataUtil> { NoDigidScreenDataUtilImpl(get(), get(), get()) }
    factory<StringUtil> { StringUtilImpl(get()) }
    factory<YourEventsEndStateUtil> { YourEventsEndStateUtilImpl(get()) }
    factory<RemovedEventsBottomSheetUtil> { RemovedEventsBottomSheetUtilImpl(get(), get(), get(), get(), get(), get()) }
    factory<StringDataZipper> { StringDataZipperImpl() }
}
