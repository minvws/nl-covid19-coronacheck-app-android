package nl.rijksoverheid.ctr.holder.modules

import nl.rijksoverheid.ctr.holder.ui.create_qr.util.*
import nl.rijksoverheid.ctr.holder.ui.myoverview.utils.QrCodeUtil
import nl.rijksoverheid.ctr.holder.ui.myoverview.utils.QrCodeUtilImpl
import nl.rijksoverheid.ctr.holder.ui.myoverview.utils.MyOverviewGreenCardExpiryUtil
import nl.rijksoverheid.ctr.holder.ui.myoverview.utils.MyOverviewGreenCardExpiryUtilImpl
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
val cardUtilsModule = module {
    factory<QrCodeUtil> { QrCodeUtilImpl() }
    factory<MyOverviewGreenCardExpiryUtil> { MyOverviewGreenCardExpiryUtilImpl(get(), androidContext()) }
    factory<InfoScreenUtil> { InfoScreenUtilImpl(get(), get(), get(), get()) }
    factory<TestInfoScreenUtil> { TestInfoScreenUtilImpl(androidContext().resources, get()) }
    factory<RecoveryInfoScreenUtil> { RecoveryInfoScreenUtilImpl(androidContext().resources) }
    factory<QrInfoScreenUtil> { QrInfoScreenUtilImpl(get(), get(), get(), get(), get()) }
    factory<VaccinationInfoScreenUtil> {
        VaccinationInfoScreenUtilImpl(get(), androidContext().resources, get(), get())
    }
    factory<LastVaccinationDoseUtil> { LastVaccinationDoseUtilImpl(androidContext().resources) }
    factory<GreenCardUtil> { GreenCardUtilImpl(get(), Clock.systemUTC(), get()) }
    factory<GreenCardRefreshUtil> {
        GreenCardRefreshUtilImpl(get(), get(), get(), get(), get(), get())
    }
    factory<VaccinationAssessmentInfoScreenUtil> {
        VaccinationAssessmentInfoScreenUtilImpl(get(), get())
    }
}
