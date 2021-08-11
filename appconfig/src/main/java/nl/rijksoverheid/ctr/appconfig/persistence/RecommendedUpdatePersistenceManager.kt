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
    fun getRecommendedUpdateShownSeconds(): Long
    fun saveRecommendedUpdateShownSeconds(seconds: Long)
}

class RecommendedUpdatePersistenceManagerImpl(private val sharedPreferences: SharedPreferences) :
    RecommendedUpdatePersistenceManager {

    companion object {
        private const val RECOMMENDED_UPDATE_SHOWN_EPOCH_SECONDS = "RECOMMENDED_UPDATE_SHOWN_EPOCH_SECONDS"
    }

    override fun getRecommendedUpdateShownSeconds(): Long {
        return sharedPreferences.getLong(RECOMMENDED_UPDATE_SHOWN_EPOCH_SECONDS, 0)
    }

    override fun saveRecommendedUpdateShownSeconds(seconds: Long) {
        sharedPreferences.edit().putLong(RECOMMENDED_UPDATE_SHOWN_EPOCH_SECONDS, seconds).apply()
    }
}
