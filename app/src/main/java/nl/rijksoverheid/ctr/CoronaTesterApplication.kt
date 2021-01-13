/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr

import android.annotation.SuppressLint
import android.app.Application
import net.danlew.android.joda.JodaTimeAndroid
import timber.log.Timber

class CoronaTesterApplication : Application() {

    @SuppressLint("RestrictedApi") // for WM Logger api
    override fun onCreate() {
        super.onCreate()
        JodaTimeAndroid.init(this)

        if (BuildConfig.FEATURE_LOGGING) {
            Timber.plant(Timber.DebugTree())
            Timber.plant(FileTree(getExternalFilesDir(null)))
            Timber.d("onCreate")
        }
    }


}