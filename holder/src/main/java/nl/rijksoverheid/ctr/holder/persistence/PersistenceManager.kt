package nl.rijksoverheid.ctr.holder.persistence

import android.annotation.SuppressLint
import android.content.SharedPreferences

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface PersistenceManager {
    suspend fun saveSecretKeyJson(json: String)
    suspend fun getSecretKeyJson(): String?
}

class SharedPreferencesPersistenceManager(private val sharedPreferences: SharedPreferences) :
    PersistenceManager {
    companion object {
        const val SECRET_KEY_JSON = "SECRET_KEY_JSON"
    }

    @SuppressLint("ApplySharedPref")
    override suspend fun saveSecretKeyJson(json: String) {
        sharedPreferences.edit().putString(SECRET_KEY_JSON, json).commit()
    }

    override suspend fun getSecretKeyJson(): String? {
        return sharedPreferences.getString(SECRET_KEY_JSON, null)
    }

}
