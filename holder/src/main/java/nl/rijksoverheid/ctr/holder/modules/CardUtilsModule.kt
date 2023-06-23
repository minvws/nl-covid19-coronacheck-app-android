package nl.rijksoverheid.ctr.holder.modules

import java.time.Clock
import nl.rijksoverheid.ctr.holder.dashboard.items.DashboardGreenCardAdapterItemExpiryUtil
import nl.rijksoverheid.ctr.holder.dashboard.items.DashboardGreenCardAdapterItemExpiryUtilImpl
import nl.rijksoverheid.ctr.holder.dashboard.util.GreenCardRefreshUtil
import nl.rijksoverheid.ctr.holder.dashboard.util.GreenCardRefreshUtilImpl
import nl.rijksoverheid.ctr.holder.dashboard.util.GreenCardUtil
import nl.rijksoverheid.ctr.holder.dashboard.util.GreenCardUtilImpl
import nl.rijksoverheid.ctr.holder.qrcodes.utils.LastVaccinationDoseUtil
import nl.rijksoverheid.ctr.holder.qrcodes.utils.LastVaccinationDoseUtilImpl
import nl.rijksoverheid.ctr.holder.qrcodes.utils.QrCodeUtil
import nl.rijksoverheid.ctr.holder.qrcodes.utils.QrCodeUtilImpl
import nl.rijksoverheid.ctr.holder.qrcodes.utils.QrInfoScreenUtil
import nl.rijksoverheid.ctr.holder.qrcodes.utils.QrInfoScreenUtilImpl
import nl.rijksoverheid.ctr.holder.your_events.utils.InfoScreenUtil
import nl.rijksoverheid.ctr.holder.your_events.utils.InfoScreenUtilImpl
import nl.rijksoverheid.ctr.holder.your_events.utils.RecoveryInfoScreenUtil
import nl.rijksoverheid.ctr.holder.your_events.utils.RecoveryInfoScreenUtilImpl
import nl.rijksoverheid.ctr.holder.your_events.utils.TestInfoScreenUtil
import nl.rijksoverheid.ctr.holder.your_events.utils.TestInfoScreenUtilImpl
import nl.rijksoverheid.ctr.holder.your_events.utils.VaccinationInfoScreenUtil
import nl.rijksoverheid.ctr.holder.your_events.utils.VaccinationInfoScreenUtilImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
val cardUtilsModule = module {
    factory<QrCodeUtil> { QrCodeUtilImpl() }
    factory<DashboardGreenCardAdapterItemExpiryUtil> { DashboardGreenCardAdapterItemExpiryUtilImpl(get(), androidContext()) }
    factory<InfoScreenUtil> { InfoScreenUtilImpl(get(), get(), get()) }
    factory<TestInfoScreenUtil> { TestInfoScreenUtilImpl(androidContext().resources, get(), get(), get()) }
    factory<RecoveryInfoScreenUtil> { RecoveryInfoScreenUtilImpl(androidContext().resources, get(), get()) }
    factory<QrInfoScreenUtil> { QrInfoScreenUtilImpl(get(), get(), get(), get(), get()) }
    factory<VaccinationInfoScreenUtil> {
        VaccinationInfoScreenUtilImpl(get(), androidContext().resources, get(), get(), get())
    }
    factory<LastVaccinationDoseUtil> { LastVaccinationDoseUtilImpl(androidContext().resources) }
    factory<GreenCardUtil> { GreenCardUtilImpl(get(), Clock.systemUTC(), get(), get()) }
    factory<GreenCardRefreshUtil> {
        GreenCardRefreshUtilImpl(get(), get(), get(), get(), get(), get())
    }
}
