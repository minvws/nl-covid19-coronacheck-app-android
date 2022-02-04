package nl.rijksoverheid.ctr.verifier.persistance

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy.*

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
    fun removeVerificationPolicySelectionSet()
    fun getRandomKey(): String?
    fun saveRandomKey(key: String)
    fun getLastScanLockTimeSeconds(): Long
    fun storeLastScanLockTimeSeconds(seconds: Long)
    fun getEnabledPolicies(): List<VerificationPolicy>
    fun setEnabledPolicies(policies: List<VerificationPolicy>)
    fun getNewPolicyRulesSeen(): Boolean
    fun setNewPolicyRulesSeen(hasSeen: Boolean)
}

class SharedPreferencesPersistenceManager(
    private val sharedPreferences: SharedPreferences,
    private val moshi: Moshi
) :
    PersistenceManager {

    companion object {
        const val SCAN_INSTRUCTIONS_SEEN = "SCAN_INSTRUCTIONS_SEEN"
        const val SECRET_KEY_JSON = "SECRET_KEY_JSON"
        const val LOCAL_TEST_RESULT = "LOCAL_TEST_RESULT"
        const val VERIFICATION_POLICY_SET = "VERIFICATION_POLICY_SET"
        const val RANDOM_KEY = "RANDOM_KEY"
        const val LAST_SCAN_LOCK_TIME_SECONDS = "LAST_SCAN_LOCK_TIME_SECONDS"
        const val ENABLED_POLICIES = "ENABLED_POLICIES"
        const val NEW_POLICY_RULES_SEEN = "NEW_POLICY_RULES_SEEN"
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

    @SuppressLint("ApplySharedPref") // Should be done synchronously as it's done right before restart of app
    override fun removeVerificationPolicySelectionSet() {
        sharedPreferences.edit().remove(VERIFICATION_POLICY_SET).commit()
    }

    override fun getRandomKey(): String? {
        return sharedPreferences.getString(RANDOM_KEY, "")
    }

    override fun saveRandomKey(key: String) {
        sharedPreferences.edit().putString(RANDOM_KEY, key).apply()
    }

    override fun getLastScanLockTimeSeconds(): Long {
        return sharedPreferences.getLong(LAST_SCAN_LOCK_TIME_SECONDS, 0L)
    }

    override fun storeLastScanLockTimeSeconds(seconds: Long) {
        sharedPreferences.edit().putLong(LAST_SCAN_LOCK_TIME_SECONDS, seconds).apply()
    }

    override fun getEnabledPolicies(): List<VerificationPolicy> {
        val type = Types.newParameterizedType(List::class.java, VerificationPolicy::class.java)
        val adapter = moshi.adapter<List<VerificationPolicy>>(type)
        val policies = sharedPreferences.getString(ENABLED_POLICIES, adapter.toJson(listOf(VerificationPolicy3G)))
        return policies?.let { adapter.fromJson(policies) } ?: listOf(VerificationPolicy3G)
    }


    @SuppressLint("ApplySharedPref") // Should be done synchronously as it's done right before restart of app
    override fun setEnabledPolicies(policies: List<VerificationPolicy>) {
        val type = Types.newParameterizedType(List::class.java, VerificationPolicy::class.java)
        val adapter = moshi.adapter<List<VerificationPolicy>>(type)
        sharedPreferences.edit().putString(ENABLED_POLICIES, adapter.toJson(policies)).commit()
    }

    override fun getNewPolicyRulesSeen(): Boolean {
        return sharedPreferences.getBoolean(NEW_POLICY_RULES_SEEN, true)
    }

    @SuppressLint("ApplySharedPref") // Should be done synchronously as it's done right before restart of app
    override fun setNewPolicyRulesSeen(hasSeen: Boolean) {
        sharedPreferences.edit().putBoolean(NEW_POLICY_RULES_SEEN, hasSeen).commit()
    }
}
