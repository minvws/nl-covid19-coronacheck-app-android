package nl.rijksoverheid.ctr.appconfig.persistence

import android.content.SharedPreferences


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

interface AppConfigPersistenceManager {
    fun getAppConfigLastFetchedSeconds(): Long
    fun saveAppConfigLastFetchedSeconds(seconds: Long)
}

class AppConfigPersistenceManagerImpl(private val sharedPreferences: SharedPreferences) :
    AppConfigPersistenceManager {

    companion object {
        const val APP_CONFIG_LAST_FETCHED_SECONDS = "APP_CONFIG_LAST_FETCHED_SECONDS"
    }

    override fun getAppConfigLastFetchedSeconds(): Long {
        return sharedPreferences.getLong(APP_CONFIG_LAST_FETCHED_SECONDS, 0)
    }

    override fun saveAppConfigLastFetchedSeconds(seconds: Long) {
        sharedPreferences.edit().putLong(APP_CONFIG_LAST_FETCHED_SECONDS, seconds).apply()
    }
}
