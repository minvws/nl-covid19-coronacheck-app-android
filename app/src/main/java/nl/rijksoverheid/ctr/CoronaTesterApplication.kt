/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import nl.rijksoverheid.ctr.holder.holderModule
import nl.rijksoverheid.ctr.shared.sharedModule
import nl.rijksoverheid.ctr.verifier.verifierModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class CoronaTesterApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        AndroidThreeTen.init(this)
        startKoin {
            androidContext(this@CoronaTesterApplication)
            modules(sharedModule, holderModule, verifierModule)
        }

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
