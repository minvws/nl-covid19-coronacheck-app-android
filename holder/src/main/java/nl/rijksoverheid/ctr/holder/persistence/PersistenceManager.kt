package nl.rijksoverheid.ctr.holder.persistence

import android.content.SharedPreferences

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface PersistenceManager {
    fun saveSecretKeyJson(json: String)
    fun getSecretKeyJson(): String?
    fun saveCredentials(credentials: String)
    fun getCredentials(): String?
    fun deleteCredentials()
    fun saveDateOfBirthMillis(millis: Long)
    fun getDateOfBirthMillis(): Long?
}

class SharedPreferencesPersistenceManager(
    private val sharedPreferences: SharedPreferences
) :
    PersistenceManager {

    companion object {
        const val SECRET_KEY_JSON = "SECRET_KEY_JSON"
        const val CREDENTIALS = "CREDENTIALS"
        const val DATE_OF_BIRTH_MILLIS = "DATE_OF_BIRTH_MILLIS"
    }

    override fun saveSecretKeyJson(json: String) {
        sharedPreferences.edit().putString(SECRET_KEY_JSON, json).apply()
    }

    override fun getSecretKeyJson(): String? {
        return sharedPreferences.getString(SECRET_KEY_JSON, null)
    }

    override fun saveCredentials(credentials: String) {
        sharedPreferences.edit().putString(CREDENTIALS, credentials).apply()
    }

    override fun getCredentials(): String? {
        return sharedPreferences.getString(CREDENTIALS, null)
    }

    override fun deleteCredentials() {
        sharedPreferences.edit().remove(CREDENTIALS).apply()
    }

    override fun saveDateOfBirthMillis(millis: Long) {
        sharedPreferences.edit().putLong(DATE_OF_BIRTH_MILLIS, millis).apply()
    }

    override fun getDateOfBirthMillis(): Long? {
        val millis = sharedPreferences.getLong(DATE_OF_BIRTH_MILLIS, 0)
        return if (millis == 0L) {
            null
        } else {
            millis
        }
    }
}
