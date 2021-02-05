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
    fun saveOnboardingFinished()
    fun getOnboardingFinished(): Boolean
    fun saveSecretKeyJson(json: String)
    fun getSecretKeyJson(): String?
}

class SharedPreferencesPersistenceManager(private val sharedPreferences: SharedPreferences) :
    PersistenceManager {

    companion object {
        const val ONBOARDING_FINISHED = "ONBOARDING_FINISHED"
        const val SECRET_KEY_JSON = "SECRET_KEY_JSON"
    }

    override fun saveOnboardingFinished() {
        sharedPreferences.edit().putBoolean(ONBOARDING_FINISHED, true).apply()
    }

    override fun getOnboardingFinished(): Boolean {
        return sharedPreferences.getBoolean(ONBOARDING_FINISHED, false)
    }

    override fun saveSecretKeyJson(json: String) {
        sharedPreferences.edit().putString(SECRET_KEY_JSON, json).apply()
    }

    override fun getSecretKeyJson(): String? {
        return sharedPreferences.getString(SECRET_KEY_JSON, null)
    }
}
