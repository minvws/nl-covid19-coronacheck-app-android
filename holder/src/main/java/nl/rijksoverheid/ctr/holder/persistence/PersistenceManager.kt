package nl.rijksoverheid.ctr.holder.persistence

import android.content.SharedPreferences
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy

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
    fun hasSeenCameraRationale(): Boolean?
    fun setHasSeenCameraRationale(hasSeen: Boolean)
    fun hasDismissedRootedDeviceDialog(): Boolean
    fun setHasDismissedRootedDeviceDialog()
    fun getSelectedDashboardTab(): Int
    fun setSelectedDashboardTab(position: Int)
    fun hasAppliedJune28Fix(): Boolean
    fun setJune28FixApplied(applied: Boolean)
    fun hasDismissedUnsecureDeviceDialog(): Boolean
    fun setHasDismissedUnsecureDeviceDialog(value : Boolean)
    fun showSyncGreenCardsItem(): Boolean
    fun setShowSyncGreenCardsItem(show: Boolean)
    fun getCheckNewValidityInfoCard(): Boolean
    fun setCheckNewValidityInfoCard(check: Boolean)
    fun getHasDismissedNewValidityInfoCard(): Boolean
    fun setHasDismissedNewValidityInfoCard(dismissed: Boolean)
    fun getHasDismissedBoosterInfoCard(): Long
    fun setHasDismissedBoosterInfoCard(dismissedAtEpochSeconds: Long)
    fun getPolicyBannerDismissed(): DisclosurePolicy?
    fun setPolicyBannerDismissed(policy: DisclosurePolicy)
    fun getPolicyScreenSeen(): DisclosurePolicy?
    fun setPolicyScreenSeen(policy: DisclosurePolicy)
}

class SharedPreferencesPersistenceManager(
    private val sharedPreferences: SharedPreferences
) :
    PersistenceManager {

    companion object {
        const val SECRET_KEY_JSON = "SECRET_KEY_JSON"
        const val CREDENTIALS = "CREDENTIALS"
        const val HAS_SEEN_CAMERA_RATIONALE = "HAS_SEEN_CAMERA_RATIONALE"
        const val HAS_SEEN_ROOTED_DEVICE_DIALOG = "HAS_SEEN_ROOTED_DEVICE_DIALOG"
        const val FIX28JUNE_APPLIED = "FIX_28_JUNE_APPLIED"
        const val SELECTED_DASHBOARD_TAB = "SELECTED_DASHBOARD_TAB"
        const val HAS_SEEN_SECURE_DEVICE_DIALOG = "HAS_SEEN_SECURE_DEVICE_DIALOG"
        const val SHOW_SYNC_GREEN_CARDS_ITEM = "SHOW_SYNC_GREEN_CARDS_ITEM"
        const val SHOULD_CHECK_RECOVERY_GREEN_CARD_REVISED_VALIDITY = "SHOULD_CHECK_RECOVERY_GREEN_CARD_REVISED_VALIDITY"
        const val SHOW_EXTEND_DOMESTIC_RECOVERY_INFO_CARD = "SHOW_EXTEND_DOMESTIC_RECOVERY_INFO_CARD"
        const val SHOW_RECOVER_DOMESTIC_RECOVERY_INFO_CARD = "SHOW_RECOVERED_DOMESTIC_RECOVERY_INFO_CARD"
        const val HAS_DISMISSED_EXTENDED_DOMESTIC_RECOVERY_INFO_CARD = "HAS_DISMISSED_EXTENDED_DOMESTIC_RECOVERY_INFO_CARD"
        const val HAS_DISMISSED_RECOVERED_DOMESTIC_RECOVERY_INFO_CARD = "HAS_DISMISSED_RECOVERED_DOMESTIC_RECOVERY_INFO_CARD"
        const val CHECK_VALIDITY_INFO_CARD = "CHECK_VALIDITY_INFO_CARD"
        const val HAS_DISMISSED_VALIDITY_INFO_CARD = "HAS_DISMISSED_VALIDITY_INFO_CARD"
        const val HAS_DISMISSED_BOOSTER_INFO_CARD = "HAS_DISMISSED_BOOSTER_INFO_CARD"
        const val POLICY_BANNER_DISMISSED = "POLICY_BANNER_DISMISSED"
        const val POLICY_SCREEN_SEEN = "POLICY_SCREEN_SEEN"
    }

    override fun saveSecretKeyJson(json: String) {
        sharedPreferences.edit().putString(SECRET_KEY_JSON, json).commit()
    }

    override fun getSecretKeyJson(): String? {
        return sharedPreferences.getString(SECRET_KEY_JSON, null)
    }

    override fun saveCredentials(credentials: String) {
        sharedPreferences.edit().putString(CREDENTIALS, credentials).commit()
    }

    override fun getCredentials(): String? {
        return sharedPreferences.getString(CREDENTIALS, null)
    }

    override fun deleteCredentials() {
        sharedPreferences.edit().remove(CREDENTIALS).commit()
    }

    override fun hasSeenCameraRationale(): Boolean {
        return sharedPreferences.getBoolean(HAS_SEEN_CAMERA_RATIONALE, false)
    }

    override fun setHasSeenCameraRationale(hasSeen: Boolean) {
        sharedPreferences.edit().putBoolean(HAS_SEEN_CAMERA_RATIONALE, hasSeen).commit()
    }

    override fun hasDismissedRootedDeviceDialog(): Boolean {
        return sharedPreferences.getBoolean(HAS_SEEN_ROOTED_DEVICE_DIALOG, false)
    }

    override fun setHasDismissedRootedDeviceDialog() {
        sharedPreferences.edit().putBoolean(HAS_SEEN_ROOTED_DEVICE_DIALOG, true).commit()
    }

    override fun getSelectedDashboardTab(): Int {
        return sharedPreferences.getInt(SELECTED_DASHBOARD_TAB, 0)
    }

    override fun setSelectedDashboardTab(position: Int) {
        sharedPreferences.edit().putInt(SELECTED_DASHBOARD_TAB, position).commit()
    }

    override fun hasAppliedJune28Fix(): Boolean {
        return sharedPreferences.getBoolean(FIX28JUNE_APPLIED, false)
    }

    override fun setJune28FixApplied(applied: Boolean) {
        sharedPreferences.edit().putBoolean(FIX28JUNE_APPLIED, applied).commit()
    }

    override fun hasDismissedUnsecureDeviceDialog(): Boolean {
        return sharedPreferences.getBoolean(HAS_SEEN_SECURE_DEVICE_DIALOG, false)
    }

    override fun setHasDismissedUnsecureDeviceDialog(value : Boolean) {
        sharedPreferences.edit().putBoolean(HAS_SEEN_SECURE_DEVICE_DIALOG, value).commit()
    }

    override fun showSyncGreenCardsItem(): Boolean {
        return sharedPreferences.getBoolean(SHOW_SYNC_GREEN_CARDS_ITEM, true)
    }

    override fun setShowSyncGreenCardsItem(show: Boolean) {
        sharedPreferences.edit().putBoolean(SHOW_SYNC_GREEN_CARDS_ITEM, show).commit()
    }

    override fun getCheckNewValidityInfoCard(): Boolean {
        return sharedPreferences.getBoolean(CHECK_VALIDITY_INFO_CARD, true)
    }

    override fun setCheckNewValidityInfoCard(check: Boolean) {
        sharedPreferences.edit().putBoolean(CHECK_VALIDITY_INFO_CARD, check).apply()
    }

    override fun getHasDismissedNewValidityInfoCard(): Boolean {
        return sharedPreferences.getBoolean(HAS_DISMISSED_VALIDITY_INFO_CARD, true)
    }

    override fun setHasDismissedNewValidityInfoCard(dismissed: Boolean) {
        sharedPreferences.edit().putBoolean(HAS_DISMISSED_VALIDITY_INFO_CARD, dismissed).apply()
    }

    override fun getHasDismissedBoosterInfoCard(): Long {
        return sharedPreferences.getLong(HAS_DISMISSED_BOOSTER_INFO_CARD, 0)
    }

    override fun setHasDismissedBoosterInfoCard(dismissedAtEpochSeconds: Long) {
        sharedPreferences.edit().putLong(HAS_DISMISSED_BOOSTER_INFO_CARD, dismissedAtEpochSeconds).apply()
    }

    override fun getPolicyBannerDismissed(): DisclosurePolicy? {
        val value = sharedPreferences.getString(POLICY_BANNER_DISMISSED, "") ?: ""
        return DisclosurePolicy.fromString(value)
    }

    override fun setPolicyBannerDismissed(policy: DisclosurePolicy) {
        sharedPreferences.edit().putString(POLICY_BANNER_DISMISSED, policy.stringValue).apply()
    }

    override fun getPolicyScreenSeen(): DisclosurePolicy? {
        val value = sharedPreferences.getString(POLICY_SCREEN_SEEN, "") ?: ""
        return DisclosurePolicy.fromString(value)
    }

    override fun setPolicyScreenSeen(policy: DisclosurePolicy) {
        sharedPreferences.edit().putString(POLICY_SCREEN_SEEN, policy.stringValue).apply()
    }
}
