package nl.rijksoverheid.ctr.holder.persistence

import android.content.SharedPreferences
import com.squareup.moshi.Moshi

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface PersistenceManager {
    fun saveOnboardingFinished()
    fun getOnboardingFinished(): Boolean
    fun savePrivacyPolicyFinished()
    fun getPrivacyPolicyFinished(): Boolean
    fun saveSecretKeyJson(json: String)
    fun getSecretKeyJson(): String?
    fun saveCredentials(credentials: String)
    fun getCredentials(): String?
    fun deleteCredentials()
}

class SharedPreferencesPersistenceManager(
    private val sharedPreferences: SharedPreferences,
    private val moshi: Moshi
) :
    PersistenceManager {

    companion object {
        const val ONBOARDING_FINISHED = "ONBOARDING_FINISHED"
        const val PRIVACY_POLICY_FINISHED = "PRIVACY_POLICY_FINISHED"
        const val SECRET_KEY_JSON = "SECRET_KEY_JSON"
        const val CREDENTIALS = "CREDENTIALS"
    }

    override fun saveOnboardingFinished() {
        sharedPreferences.edit().putBoolean(ONBOARDING_FINISHED, true).apply()
    }

    override fun getOnboardingFinished(): Boolean {
        return sharedPreferences.getBoolean(ONBOARDING_FINISHED, false)
    }

    override fun savePrivacyPolicyFinished() {
        sharedPreferences.edit().putBoolean(PRIVACY_POLICY_FINISHED, true).apply()
    }

    override fun getPrivacyPolicyFinished(): Boolean {
        return sharedPreferences.getBoolean(PRIVACY_POLICY_FINISHED, false)
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
}
