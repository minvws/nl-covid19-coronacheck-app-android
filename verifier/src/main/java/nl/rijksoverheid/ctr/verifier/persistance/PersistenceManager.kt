package nl.rijksoverheid.ctr.verifier.persistance

import android.content.SharedPreferences

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface PersistenceManager {
    fun getScanInstructionsSeen(): Boolean
    fun setScanInstructionsSeen()
    fun saveSecretKeyJson(json: String)
    fun getSecretKeyJson(): String?
    fun saveLocalTestResultJson(localTestResultJson: String)
    fun getLocalTestResultJson(): String?
    fun saveEuPublicKeyPath(path: String)
    fun getEuPublicKeyPath() : String?
    fun getEuPublicKeysLastFetchedSeconds(): Long
    fun saveEuPublicKeysLastFetchedSeconds(seconds: Long)
}

class SharedPreferencesPersistenceManager(private val sharedPreferences: SharedPreferences) :
    PersistenceManager {

    companion object {
        const val SCAN_INSTRUCTIONS_SEEN = "SCAN_INSTRUCTIONS_SEEN"
        const val SECRET_KEY_JSON = "SECRET_KEY_JSON"
        const val LOCAL_TEST_RESULT = "LOCAL_TEST_RESULT"
        const val EU_PUBLIC_KEYS_PATH = "EU_PUBLIC_KEYS_PATH"
        const val EU_PUBLIC_KEYS_LAST_FETCHED_SECONDS = "EU_PUBLIC_KEYS_LAST_FETCHED_SECONDS"
    }

    override fun getScanInstructionsSeen(): Boolean {
        return sharedPreferences.getBoolean(SCAN_INSTRUCTIONS_SEEN, false)
    }

    override fun setScanInstructionsSeen() {
        sharedPreferences.edit().putBoolean(SCAN_INSTRUCTIONS_SEEN, true).apply()
    }

    override fun saveSecretKeyJson(json: String) {
        sharedPreferences.edit().putString(SECRET_KEY_JSON, json).apply()
    }

    override fun getSecretKeyJson(): String? {
        return sharedPreferences.getString(SECRET_KEY_JSON, null)
    }

    override fun saveLocalTestResultJson(localTestResultJson: String) {
        sharedPreferences.edit().putString(LOCAL_TEST_RESULT, localTestResultJson).apply()
    }

    override fun getLocalTestResultJson(): String? {
        return sharedPreferences.getString(LOCAL_TEST_RESULT, null)
    }

    override fun saveEuPublicKeyPath(path : String) {
        sharedPreferences.edit().putString(EU_PUBLIC_KEYS_PATH, path).apply()
    }

    override fun getEuPublicKeyPath() : String? {
        return sharedPreferences.getString(EU_PUBLIC_KEYS_PATH, null)
    }


    override fun getEuPublicKeysLastFetchedSeconds(): Long {
        return sharedPreferences.getLong(EU_PUBLIC_KEYS_LAST_FETCHED_SECONDS, 0L)
    }

    override fun saveEuPublicKeysLastFetchedSeconds(seconds: Long) {
        sharedPreferences.edit().putLong(EU_PUBLIC_KEYS_LAST_FETCHED_SECONDS, seconds).apply()
    }
}
