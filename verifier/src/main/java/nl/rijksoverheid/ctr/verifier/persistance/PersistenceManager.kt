package nl.rijksoverheid.ctr.verifier.persistance

import android.content.SharedPreferences
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface PersistenceManager {
    fun setScanInstructionsSeen()
    fun getScanInstructionsSeen(): Boolean
    fun saveSecretKeyJson(json: String)
    fun getSecretKeyJson(): String?
    fun saveLocalTestResultJson(localTestResultJson: String)
    fun getLocalTestResultJson(): String?
    fun setVerificationPolicySelected(policy: VerificationPolicy)
    fun getVerificationPolicySelected(): VerificationPolicy?
    fun isVerificationPolicySelectionSet(): Boolean
}

class SharedPreferencesPersistenceManager(private val sharedPreferences: SharedPreferences) :
    PersistenceManager {

    companion object {
        const val SCAN_INSTRUCTIONS_SEEN = "SCAN_INSTRUCTIONS_SEEN"
        const val SECRET_KEY_JSON = "SECRET_KEY_JSON"
        const val LOCAL_TEST_RESULT = "LOCAL_TEST_RESULT"
        const val VERIFICATION_POLICY_SET = "VERIFICATION_POLICY_SET"
    }

    override fun setScanInstructionsSeen() {
        sharedPreferences.edit().putBoolean(SCAN_INSTRUCTIONS_SEEN, true).apply()
    }

    override fun getScanInstructionsSeen(): Boolean {
        return sharedPreferences.getBoolean(SCAN_INSTRUCTIONS_SEEN, false)
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

    override fun setVerificationPolicySelected(policy: VerificationPolicy) {
        sharedPreferences.edit().putString(VERIFICATION_POLICY_SET, policy.libraryValue).apply()
    }

    override fun getVerificationPolicySelected(): VerificationPolicy? {
        return VerificationPolicy.fromString(
            sharedPreferences.getString(
                VERIFICATION_POLICY_SET,
                null
            )
        )
    }

    override fun isVerificationPolicySelectionSet(): Boolean {
        return sharedPreferences.contains(VERIFICATION_POLICY_SET)
    }
}
