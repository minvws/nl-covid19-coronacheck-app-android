package nl.rijksoverheid.ctr.holder.modules

import nl.rijksoverheid.ctr.holder.ui.create_qr.util.*
import nl.rijksoverheid.ctr.holder.ui.myoverview.items.MyOverViewGreenCardAdapterUtil
import nl.rijksoverheid.ctr.holder.ui.myoverview.items.MyOverViewGreenCardAdapterUtilImpl
import nl.rijksoverheid.ctr.holder.ui.myoverview.utils.TokenValidatorUtil
import nl.rijksoverheid.ctr.holder.ui.myoverview.utils.TokenValidatorUtilImpl
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
val utilsModule = module {
    factory<MyOverViewGreenCardAdapterUtil> {
        MyOverViewGreenCardAdapterUtilImpl(
            androidContext(),
            get(),
            get(),
            get()
        )
    }
    factory<TokenValidatorUtil> { TokenValidatorUtilImpl() }
    factory<CredentialUtil> { CredentialUtilImpl(Clock.systemUTC(), get(), get()) }
    factory<OriginUtil> { OriginUtilImpl(Clock.systemUTC()) }
    factory<RemoteEventRecoveryUtil> { RemoteEventRecoveryUtilImpl(get()) }
    factory<RemoteEventHolderUtil> { RemoteEventHolderUtilImpl(get(), get()) }
    factory<RemoteProtocol3Util> { RemoteProtocol3UtilImpl() }
    factory<RemoteEventUtil> { RemoteEventUtilImpl() }
    factory<ReadEuropeanCredentialUtil> { ReadEuropeanCredentialUtilImpl(get()) }
    factory<DashboardItemUtil> { DashboardItemUtilImpl(get(), get(), get(), get(), get()) }
    factory<CountryUtil> { CountryUtilImpl() }
    factory<MultipleQrCodesUtil> { MultipleQrCodesUtilImpl() }
}
