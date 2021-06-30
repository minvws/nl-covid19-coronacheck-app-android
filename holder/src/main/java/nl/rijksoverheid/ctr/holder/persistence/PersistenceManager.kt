package nl.rijksoverheid.ctr.holder.persistence

import android.content.SharedPreferences
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType

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
    fun getSelectedGreenCardType(): GreenCardType
    fun setSelectedGreenCardType(greenCardType: GreenCardType)
    fun hasAppliedJune28Fix(): Boolean
    fun setJune28FixApplied(applied: Boolean)
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
        const val SELECTED_GREEN_CARD_TYPE = "SELECTED_GREEN_CARD_TYPE"
        const val FIX28JUNE_APPLIED = "FIX_28_JUNE_APPLIED"
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

    override fun getSelectedGreenCardType(): GreenCardType {
        val type = sharedPreferences.getString(SELECTED_GREEN_CARD_TYPE, GreenCardType.TYPE_DOMESTIC)
        return if (type == GreenCardType.TYPE_DOMESTIC) {
            GreenCardType.Domestic
        } else {
            GreenCardType.Eu
        }
    }

    override fun setSelectedGreenCardType(greenCardType: GreenCardType) {
        val typeString = when (greenCardType) {
            is GreenCardType.Domestic -> GreenCardType.TYPE_DOMESTIC
            is GreenCardType.Eu -> GreenCardType.TYPE_EU
        }
        val result = sharedPreferences.edit().putString(SELECTED_GREEN_CARD_TYPE, typeString).commit()
        if (!result) {
            throw IllegalStateException("Failed to set selected green card type in shared preference")
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
}
