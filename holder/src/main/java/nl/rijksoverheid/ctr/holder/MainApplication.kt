package nl.rijksoverheid.ctr.holder

import nl.rijksoverheid.ctr.appconfig.appConfigModule
import nl.rijksoverheid.ctr.shared.SharedApplication
import nl.rijksoverheid.ctr.shared.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class MainApplication : SharedApplication() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MainApplication)
            modules(mainModule, sharedModule, appConfigModule("holder", BuildConfig.VERSION_CODE))
        }
    }
}
