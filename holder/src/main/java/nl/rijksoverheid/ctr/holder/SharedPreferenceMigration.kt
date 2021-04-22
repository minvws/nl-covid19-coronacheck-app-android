package nl.rijksoverheid.ctr.holder

import android.content.SharedPreferences
import androidx.core.content.edit
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.persistence.SharedPreferencesPersistenceManager
import nl.rijksoverheid.ctr.introduction.persistance.IntroductionPersistenceManager

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

/**
 * Temporary class to migrate from shared preferences to encrypted shared preferences
 */
class SharedPreferenceMigration(
    private val oldSharedPreferences: SharedPreferences,
    private val newSharedPreferences: SharedPreferences
) {

    /**
     * If a user skipped the onboarding it has saved preferences in old shared preferences, so migrate
     */
    fun migrate() {
        // Only migrate if we finished the onboarding before and we don't have credentials stored
        if (oldSharedPreferences.getBoolean(
                IntroductionPersistenceManager.INTRODUCTION_FINISHED,
                false
            ) && newSharedPreferences.getString(SharedPreferencesPersistenceManager.CREDENTIALS, null) == null
        ) {
            for (entry in oldSharedPreferences.all.entries) {
                val key = entry.key
                val value: Any? = entry.value
                newSharedPreferences.set(key, value)
            }

            // Only run migration once
            oldSharedPreferences.set(IntroductionPersistenceManager.INTRODUCTION_FINISHED, false)
        }
    }
}

fun SharedPreferences.set(key: String, value: Any?) {
    when (value) {
        is String? -> edit { putString(key, value) }
        is Int -> edit { putInt(key, value.toInt()) }
        is Boolean -> edit { putBoolean(key, value) }
        is Float -> edit { putFloat(key, value.toFloat()) }
        is Long -> edit { putLong(key, value.toLong()) }
        else -> {
        }
    }
}
