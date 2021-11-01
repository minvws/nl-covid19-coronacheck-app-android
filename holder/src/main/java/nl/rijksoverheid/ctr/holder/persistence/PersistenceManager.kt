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
    fun hasDismissedSyncedGreenCardsItem(): Boolean
    fun setHasDismissedSyncedGreenCardsItem(dismissed: Boolean)
    fun showSyncGreenCardsItem(): Boolean
    fun setShowSyncGreenCardsItem(show: Boolean)
    fun setShouldCheckRecoveryGreenCardRevisedValidity(check: Boolean)
    fun getShouldCheckRecoveryGreenCardRevisedValidity(): Boolean
    fun setShowExtendDomesticRecoveryInfoCard(show: Boolean)
    fun getShowExtendDomesticRecoveryInfoCard(): Boolean
    fun setShowRecoverDomesticRecoveryInfoCard(show: Boolean)
    fun getShowRecoverDomesticRecoveryInfoCard(): Boolean
    fun setHasDismissedExtendedDomesticRecoveryInfoCard(dismissed: Boolean)
    fun getHasDismissedExtendedDomesticRecoveryInfoCard(): Boolean
    fun setHasDismissedRecoveredDomesticRecoveryInfoCard(dismissed: Boolean)
    fun getHasDismissedRecoveredDomesticRecoveryInfoCard(): Boolean
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
        const val HAS_DISMISSED_SYNCED_GREEN_CARDS_ITEM = "HAS_DISMISSED_SYNCED_GREEN_CARDS_ITEM"
        const val SHOW_SYNC_GREEN_CARDS_ITEM = "SHOW_SYNC_GREEN_CARDS_ITEM"
        const val SHOULD_CHECK_RECOVERY_GREEN_CARD_REVISED_VALIDITY = "SHOULD_CHECK_RECOVERY_GREEN_CARD_REVISED_VALIDITY"
        const val SHOW_EXTEND_DOMESTIC_RECOVERY_INFO_CARD = "SHOW_EXTEND_DOMESTIC_RECOVERY_INFO_CARD"
        const val SHOW_RECOVER_DOMESTIC_RECOVERY_INFO_CARD = "SHOW_RECOVERED_DOMESTIC_RECOVERY_INFO_CARD"
        const val HAS_DISMISSED_EXTENDED_DOMESTIC_RECOVERY_INFO_CARD = "HAS_DISMISSED_EXTENDED_DOMESTIC_RECOVERY_INFO_CARD"
        const val HAS_DISMISSED_RECOVERED_DOMESTIC_RECOVERY_INFO_CARD = "HAS_DISMISSED_RECOVERED_DOMESTIC_RECOVERY_INFO_CARD"
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

    override fun hasDismissedSyncedGreenCardsItem(): Boolean {
        return sharedPreferences.getBoolean(HAS_DISMISSED_SYNCED_GREEN_CARDS_ITEM, true)
    }

    override fun setHasDismissedSyncedGreenCardsItem(dismissed: Boolean) {
        sharedPreferences.edit().putBoolean(HAS_DISMISSED_SYNCED_GREEN_CARDS_ITEM, dismissed).commit()
    }

    override fun showSyncGreenCardsItem(): Boolean {
        return sharedPreferences.getBoolean(SHOW_SYNC_GREEN_CARDS_ITEM, true)
    }

    override fun setShowSyncGreenCardsItem(show: Boolean) {
        sharedPreferences.edit().putBoolean(SHOW_SYNC_GREEN_CARDS_ITEM, show).commit()
    }

    override fun setShouldCheckRecoveryGreenCardRevisedValidity(check: Boolean) {
        sharedPreferences.edit().putBoolean(SHOULD_CHECK_RECOVERY_GREEN_CARD_REVISED_VALIDITY, check).commit()
    }

    override fun getShouldCheckRecoveryGreenCardRevisedValidity(): Boolean {
        return sharedPreferences.getBoolean(SHOULD_CHECK_RECOVERY_GREEN_CARD_REVISED_VALIDITY, true)
    }

    override fun setShowExtendDomesticRecoveryInfoCard(show: Boolean) {
        sharedPreferences.edit().putBoolean(SHOW_EXTEND_DOMESTIC_RECOVERY_INFO_CARD, show).commit()
    }

    override fun getShowExtendDomesticRecoveryInfoCard(): Boolean {
        return sharedPreferences.getBoolean(SHOW_EXTEND_DOMESTIC_RECOVERY_INFO_CARD, false)
    }

    override fun setShowRecoverDomesticRecoveryInfoCard(show: Boolean) {
        sharedPreferences.edit().putBoolean(SHOW_RECOVER_DOMESTIC_RECOVERY_INFO_CARD, show).commit()
    }

    override fun getShowRecoverDomesticRecoveryInfoCard(): Boolean {
        return sharedPreferences.getBoolean(SHOW_RECOVER_DOMESTIC_RECOVERY_INFO_CARD, false)
    }

    override fun setHasDismissedExtendedDomesticRecoveryInfoCard(dismissed: Boolean) {
        sharedPreferences.edit().putBoolean(HAS_DISMISSED_EXTENDED_DOMESTIC_RECOVERY_INFO_CARD, dismissed).commit()
    }

    override fun getHasDismissedExtendedDomesticRecoveryInfoCard(): Boolean {
        return sharedPreferences.getBoolean(HAS_DISMISSED_EXTENDED_DOMESTIC_RECOVERY_INFO_CARD, true)
    }

    override fun setHasDismissedRecoveredDomesticRecoveryInfoCard(dismissed: Boolean) {
        sharedPreferences.edit().putBoolean(HAS_DISMISSED_RECOVERED_DOMESTIC_RECOVERY_INFO_CARD, dismissed).commit()
    }

    override fun getHasDismissedRecoveredDomesticRecoveryInfoCard(): Boolean {
        return sharedPreferences.getBoolean(HAS_DISMISSED_RECOVERED_DOMESTIC_RECOVERY_INFO_CARD, true)
    }
}
