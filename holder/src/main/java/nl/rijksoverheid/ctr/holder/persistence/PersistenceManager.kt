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
    }

    override fun saveSecretKeyJson(json: String) {
        val result = sharedPreferences.edit().putString(SECRET_KEY_JSON, json).commit()
        if (!result) {
            throw IllegalStateException("Failed to save secret key in shared preference")
        }
    }

    override fun getSecretKeyJson(): String? {
        return sharedPreferences.getString(SECRET_KEY_JSON, null)
    }

    override fun saveCredentials(credentials: String) {
        val result = sharedPreferences.edit().putString(CREDENTIALS, credentials).commit()
        if (!result) {
            throw IllegalStateException("Failed to save credentials in shared preference")
        }
    }

    override fun getCredentials(): String? {
        return sharedPreferences.getString(CREDENTIALS, null)
    }

    override fun deleteCredentials() {
        val result = sharedPreferences.edit().remove(CREDENTIALS).commit()
        if (!result) {
            throw IllegalStateException("Failed to delete credentials in shared preference")
        }
    }

    override fun hasSeenCameraRationale(): Boolean {
        return sharedPreferences.getBoolean(HAS_SEEN_CAMERA_RATIONALE, false)
    }

    override fun setHasSeenCameraRationale(hasSeen: Boolean) {
        val result =
            sharedPreferences.edit().putBoolean(HAS_SEEN_CAMERA_RATIONALE, hasSeen).commit()
        if (!result) {
            throw IllegalStateException("Failed to set camera rationale has been seen in shared preference")
        }
    }

    override fun hasDismissedRootedDeviceDialog(): Boolean {
        return sharedPreferences.getBoolean(HAS_SEEN_ROOTED_DEVICE_DIALOG, false)
    }

    override fun setHasDismissedRootedDeviceDialog() {
        val result =
            sharedPreferences.edit().putBoolean(HAS_SEEN_ROOTED_DEVICE_DIALOG, true).commit()
        if (!result) {
            throw IllegalStateException("Failed to set rooted device dialog has been seen in shared preference")
        }
    }

    override fun getSelectedDashboardTab(): Int {
        return sharedPreferences.getInt(SELECTED_DASHBOARD_TAB, 0)
    }

    override fun setSelectedDashboardTab(position: Int) {
        val result = sharedPreferences.edit().putInt(SELECTED_DASHBOARD_TAB, position).commit()
        if (!result) {
            throw IllegalStateException("Failed to set selected dashboard tab in shared preference")
        }
    }

    override fun hasAppliedJune28Fix(): Boolean {
        return sharedPreferences.getBoolean(FIX28JUNE_APPLIED, false)
    }

    override fun setJune28FixApplied(applied: Boolean) {
        val result =
            sharedPreferences.edit().putBoolean(FIX28JUNE_APPLIED, applied).commit()
        if (!result) {
            throw IllegalStateException("Failed to set that the june 28 fix has been applied in shared preferences")
        }
    }

    override fun hasDismissedUnsecureDeviceDialog(): Boolean {
        return sharedPreferences.getBoolean(HAS_SEEN_SECURE_DEVICE_DIALOG, false)
    }

    override fun setHasDismissedUnsecureDeviceDialog(value : Boolean) {
        val result =
            sharedPreferences.edit().putBoolean(HAS_SEEN_SECURE_DEVICE_DIALOG, value).commit()
        if (!result) {
            throw IllegalStateException("Failed to set secure device dialog has been seen in shared preference")
        }
    }

    override fun hasDismissedSyncedGreenCardsItem(): Boolean {
        return sharedPreferences.getBoolean(HAS_DISMISSED_SYNCED_GREEN_CARDS_ITEM, true)
    }

    override fun setHasDismissedSyncedGreenCardsItem(dismissed: Boolean) {
        val result =
            sharedPreferences.edit().putBoolean(HAS_DISMISSED_SYNCED_GREEN_CARDS_ITEM, dismissed).commit()
        if (!result) {
            throw IllegalStateException("Failed to set has dismissed synced green cards item in shared preference")
        }
    }

    override fun showSyncGreenCardsItem(): Boolean {
        return sharedPreferences.getBoolean(SHOW_SYNC_GREEN_CARDS_ITEM, true)
    }

    override fun setShowSyncGreenCardsItem(show: Boolean) {
        val result =
            sharedPreferences.edit().putBoolean(SHOW_SYNC_GREEN_CARDS_ITEM, show).commit()
        if (!result) {
            throw IllegalStateException("Failed to set show sync green cards item in shared preference")
        }
    }
}
