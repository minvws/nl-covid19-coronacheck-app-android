package nl.rijksoverheid.ctr.appconfig.persistence

import android.content.SharedPreferences


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

interface RecommendedUpdatePersistenceManager {
    fun getHolderVersionUpdateShown(): Int
    fun saveHolderVersionShown(version: Int)
    fun getRecommendedUpdateShownSeconds(): Long
    fun saveRecommendedUpdateShownSeconds(seconds: Long)
}

class RecommendedUpdatePersistenceManagerImpl(private val sharedPreferences: SharedPreferences) :
    RecommendedUpdatePersistenceManager {

    companion object {
        private const val HOLDER_RECOMMENDED_VERSION_SHOWN = "HOLDER_RECOMMENDED_VERSION_SHOWN"
        private const val RECOMMENDED_UPDATE_SHOWN_EPOCH_SECONDS =
            "RECOMMENDED_UPDATE_SHOWN_EPOCH_SECONDS"
    }

    override fun getHolderVersionUpdateShown(): Int {
        return sharedPreferences.getInt(HOLDER_RECOMMENDED_VERSION_SHOWN, 0)
    }

    override fun saveHolderVersionShown(version: Int) {
        sharedPreferences.edit().putInt(HOLDER_RECOMMENDED_VERSION_SHOWN, version).apply()
    }

    override fun getRecommendedUpdateShownSeconds(): Long {
        return sharedPreferences.getLong(RECOMMENDED_UPDATE_SHOWN_EPOCH_SECONDS, 0)
    }

    override fun saveRecommendedUpdateShownSeconds(seconds: Long) {
        sharedPreferences.edit().putLong(RECOMMENDED_UPDATE_SHOWN_EPOCH_SECONDS, seconds).apply()
    }
}
