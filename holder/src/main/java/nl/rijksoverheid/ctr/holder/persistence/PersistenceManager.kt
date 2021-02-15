package nl.rijksoverheid.ctr.holder.persistence

import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.holder.models.LocalTestResult
import nl.rijksoverheid.ctr.shared.ext.toObject

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
    fun saveLocalTestResult(localTestResult: LocalTestResult)
    fun getLocalTestResult(): LocalTestResult?
    fun deleteLocalTestResult()
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
        const val LOCAL_TEST_RESULT = "LOCAL_TEST_RESULT"
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

    override fun saveLocalTestResult(localTestResult: LocalTestResult) {
        sharedPreferences.edit().putString(LOCAL_TEST_RESULT, localTestResult.toJson(moshi)).apply()
    }

    override fun getLocalTestResult(): LocalTestResult? {
        return sharedPreferences.getString(LOCAL_TEST_RESULT, null)
            ?.toObject<LocalTestResult>(moshi)
    }

    override fun deleteLocalTestResult() {
        sharedPreferences.edit().remove(LOCAL_TEST_RESULT).apply()
    }
}
