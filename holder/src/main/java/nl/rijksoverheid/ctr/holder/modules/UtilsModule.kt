package nl.rijksoverheid.ctr.holder.modules

import nl.rijksoverheid.ctr.holder.dashboard.items.*
import nl.rijksoverheid.ctr.holder.dashboard.util.*
import nl.rijksoverheid.ctr.holder.get_events.utils.ScopeUtil
import nl.rijksoverheid.ctr.holder.get_events.utils.ScopeUtilImpl
import nl.rijksoverheid.ctr.holder.input_token.utils.TokenValidatorUtil
import nl.rijksoverheid.ctr.holder.input_token.utils.TokenValidatorUtilImpl
import nl.rijksoverheid.ctr.holder.no_digid.NoDigidScreenDataUtil
import nl.rijksoverheid.ctr.holder.no_digid.NoDigidScreenDataUtilImpl
import nl.rijksoverheid.ctr.holder.paper_proof.utils.PaperProofUtil
import nl.rijksoverheid.ctr.holder.paper_proof.utils.PaperProofUtilImpl
import nl.rijksoverheid.ctr.persistence.database.util.YourEventFragmentEndStateUtil
import nl.rijksoverheid.ctr.persistence.database.util.YourEventFragmentEndStateUtilImpl
import nl.rijksoverheid.ctr.holder.qrcodes.models.ReadEuropeanCredentialUtil
import nl.rijksoverheid.ctr.holder.qrcodes.models.ReadEuropeanCredentialUtilImpl
import nl.rijksoverheid.ctr.holder.qrcodes.utils.MultipleQrCodesUtil
import nl.rijksoverheid.ctr.holder.qrcodes.utils.MultipleQrCodesUtilImpl
import nl.rijksoverheid.ctr.holder.qrcodes.utils.QrCodesFragmentUtil
import nl.rijksoverheid.ctr.holder.qrcodes.utils.QrCodesFragmentUtilImpl
import nl.rijksoverheid.ctr.holder.your_events.widgets.YourEventWidgetUtil
import nl.rijksoverheid.ctr.holder.your_events.widgets.YourEventWidgetUtilImpl
import nl.rijksoverheid.ctr.holder.utils.CountryUtil
import nl.rijksoverheid.ctr.holder.utils.CountryUtilImpl
import nl.rijksoverheid.ctr.holder.utils.LocalDateUtil
import nl.rijksoverheid.ctr.holder.utils.LocalDateUtilImpl
import nl.rijksoverheid.ctr.holder.your_events.utils.*
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import java.time.Clock

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
fun utilsModule(versionCode: Int) = module {
    factory<DashboardGreenCardAdapterItemUtil> {
        DashboardGreenCardAdapterItemUtilImpl(
            Clock.systemUTC(),
            androidContext(),
            get(),
            get()
        )
    }
    factory<TokenValidatorUtil> { TokenValidatorUtilImpl() }
    factory<CredentialUtil> { CredentialUtilImpl(Clock.systemUTC(), get(), get(), get(), get()) }
    factory<OriginUtil> { OriginUtilImpl(Clock.systemUTC()) }
    factory<RemoteEventHolderUtil> { RemoteEventHolderUtilImpl(get(), get(), get(), get()) }
    factory<RemoteProtocol3Util> { RemoteProtocol3UtilImpl() }
    factory<RemoteEventUtil> { RemoteEventUtilImpl(get()) }
    factory<ReadEuropeanCredentialUtil> { ReadEuropeanCredentialUtilImpl(get()) }
    factory<DashboardItemUtil> { DashboardItemUtilImpl(get(), get(), get(), get(), get(), get()) }
    factory<CountryUtil> { CountryUtilImpl() }
    factory<LocalDateUtil> { LocalDateUtilImpl(get(), get()) }
    factory<MultipleQrCodesUtil> { MultipleQrCodesUtilImpl() }
    factory<DashboardPageInfoItemHandlerUtil> { DashboardPageInfoItemHandlerUtilImpl(get(), get(), get()) }
    factory<YourEventFragmentEndStateUtil> {
        YourEventFragmentEndStateUtilImpl(get(), get())
    }
    factory<QrCodesFragmentUtil> { QrCodesFragmentUtilImpl(Clock.systemUTC()) }
    factory<YourEventsFragmentUtil> { YourEventsFragmentUtilImpl(get()) }
    factory<YourEventWidgetUtil> { YourEventWidgetUtilImpl() }
    factory<DashboardInfoCardAdapterItemUtil> { DashboardInfoCardAdapterItemUtilImpl() }
    factory<DashboardItemEmptyStateUtil> { DashboardItemEmptyStateUtilImpl(get()) }
    factory<MenuUtil> { MenuUtilImpl(get(), get(), get()) }
    factory<ScopeUtil> { ScopeUtilImpl() }
    factory<DashboardHeaderAdapterItemUtil> { DashboardHeaderAdapterItemUtilImpl(get()) }
    factory<CardItemUtil> { CardItemUtilImpl(get(), get()) }
    factory<EventGroupEntityUtil> { EventGroupEntityUtilImpl(get()) }
    factory<PaperProofUtil> { PaperProofUtilImpl(get(), get(), get()) }
    factory<NoDigidScreenDataUtil> { NoDigidScreenDataUtilImpl(get(), get()) }
}
