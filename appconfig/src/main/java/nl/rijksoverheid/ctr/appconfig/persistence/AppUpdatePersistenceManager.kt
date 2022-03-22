package nl.rijksoverheid.ctr.appconfig.persistence

import android.content.SharedPreferences


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

interface AppUpdatePersistenceManager {
    fun getNewTermsSeen(version: Int): Boolean
    fun saveNewTermsSeen(version: Int)
    fun getNewFeaturesSeen(version: Int): Boolean
    fun saveNewFeaturesSeen(version: Int)
}

class AppUpdatePersistenceManagerImpl(private val sharedPreferences: SharedPreferences) :
    AppUpdatePersistenceManager {

    companion object {
        const val NEW_TERMS_SEEN = "NEW_TERMS_SEEN_[VERSION]"
        const val NEW_FEATURES_SEEN = "NEW_FEATURES_SEEN_[VERSION]"
    }

    override fun getNewTermsSeen(version: Int): Boolean {
        return getNewSeen(NEW_TERMS_SEEN, version)
    }

    override fun saveNewTermsSeen(version: Int) {
        saveNewSeen(NEW_TERMS_SEEN, version)
    }

    override fun getNewFeaturesSeen(version: Int): Boolean {
        return getNewSeen(NEW_FEATURES_SEEN, version)
    }

    override fun saveNewFeaturesSeen(version: Int) {
        saveNewSeen(NEW_FEATURES_SEEN, version)
    }

    private fun getNewSeen(type: String, version: Int): Boolean {
        return sharedPreferences.getBoolean(
            type.replace("[VERSION]", version.toString()),
            false
        )
    }

    private fun saveNewSeen(type: String, version: Int) {
        sharedPreferences.edit()
            .putBoolean(
                type.replace(
                    "[VERSION]",
                    version.toString(),
                ), true
            ).apply()
    }
}
