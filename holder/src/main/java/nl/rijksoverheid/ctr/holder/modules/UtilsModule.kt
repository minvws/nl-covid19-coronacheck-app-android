package nl.rijksoverheid.ctr.holder.modules

import nl.rijksoverheid.ctr.holder.dashboard.items.*
import nl.rijksoverheid.ctr.holder.dashboard.util.DashboardPageInfoItemHandlerUtil
import nl.rijksoverheid.ctr.holder.dashboard.util.DashboardPageInfoItemHandlerUtilImpl
import nl.rijksoverheid.ctr.holder.dashboard.util.MenuUtil
import nl.rijksoverheid.ctr.holder.dashboard.util.MenuUtilImpl
import nl.rijksoverheid.ctr.holder.persistence.database.util.YourEventFragmentEndStateUtil
import nl.rijksoverheid.ctr.holder.persistence.database.util.YourEventFragmentEndStateUtilImpl
import nl.rijksoverheid.ctr.holder.ui.create_qr.widgets.YourEventWidgetUtil
import nl.rijksoverheid.ctr.holder.ui.create_qr.widgets.YourEventWidgetUtilImpl
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.*
import nl.rijksoverheid.ctr.holder.ui.myoverview.utils.*
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
    factory<CredentialUtil> { CredentialUtilImpl(Clock.systemUTC(), get(), get(), get()) }
    factory<OriginUtil> { OriginUtilImpl(Clock.systemUTC()) }
    factory<RemoteEventHolderUtil> { RemoteEventHolderUtilImpl(get(), get(), get(), get()) }
    factory<RemoteProtocol3Util> { RemoteProtocol3UtilImpl() }
    factory<RemoteEventUtil> { RemoteEventUtilImpl(get()) }
    factory<ReadEuropeanCredentialUtil> { ReadEuropeanCredentialUtilImpl(get()) }
    factory<DashboardItemUtil> { DashboardItemUtilImpl(get(), get(), get(), get(), get(), get(), get()) }
    factory<CountryUtil> { CountryUtilImpl() }
    factory<MultipleQrCodesUtil> { MultipleQrCodesUtilImpl() }
    factory<DashboardPageInfoItemHandlerUtil> { DashboardPageInfoItemHandlerUtilImpl(get(), get(), get()) }
    factory<YourEventFragmentEndStateUtil> {
        YourEventFragmentEndStateUtilImpl(get())
    }
    factory<QrCodesFragmentUtil> { QrCodesFragmentUtilImpl(Clock.systemUTC()) }
    factory<YourEventsFragmentUtil> { YourEventsFragmentUtilImpl(get()) }
    factory<YourEventWidgetUtil> { YourEventWidgetUtilImpl() }
    factory<DashboardInfoCardAdapterItemUtil> { DashboardInfoCardAdapterItemUtilImpl() }
    factory<DashboardItemEmptyStateUtil> { DashboardItemEmptyStateUtilImpl(get()) }
    factory<MenuUtil> { MenuUtilImpl(get(), get()) }
    factory<ScopeUtil> { ScopeUtilImpl() }
    factory<DashboardHeaderAdapterItemUtil> { DashboardHeaderAdapterItemUtilImpl(get()) }
    factory<CardItemUtil> { CardItemUtilImpl(get(), get()) }
}
