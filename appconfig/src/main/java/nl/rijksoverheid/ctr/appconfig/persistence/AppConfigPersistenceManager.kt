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
    fun saveAppConfigJson(json: String)
    fun getAppConfigJson(): String?
    fun removeConfigJson()
    fun getAppConfigLastFetchedSeconds(): Long
    fun saveAppConfigLastFetchedSeconds(seconds: Long)
}

class AppConfigPersistenceManagerImpl(private val sharedPreferences: SharedPreferences) :
    AppConfigPersistenceManager {

    companion object {
        const val APP_CONFIG_JSON = "APP_CONFIG_JSON"
        const val APP_CONFIG_LAST_FETCHED_SECONDS = "APP_CONFIG_LAST_FETCHED_SECONDS"
    }

    override fun saveAppConfigJson(json: String) {
        sharedPreferences.edit().putString(APP_CONFIG_JSON, json).apply()
    }

    override fun getAppConfigJson(): String? {
        return sharedPreferences.getString(APP_CONFIG_JSON, null)
    }

    override fun removeConfigJson() {
        sharedPreferences.edit().remove(APP_CONFIG_JSON).apply()
    }

    override fun getAppConfigLastFetchedSeconds(): Long {
        return sharedPreferences.getLong(APP_CONFIG_LAST_FETCHED_SECONDS, 0)
    }

    override fun saveAppConfigLastFetchedSeconds(seconds: Long) {
        sharedPreferences.edit().putLong(APP_CONFIG_LAST_FETCHED_SECONDS, seconds).apply()
    }
}
