package nl.rijksoverheid.ctr.verifier

import android.content.SharedPreferences
import androidx.core.content.edit
import nl.rijksoverheid.ctr.holder.persistence.IntroductionPersistenceManager

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
        if (oldSharedPreferences.getBoolean(
                IntroductionPersistenceManager.INTRODUCTION_FINISHED,
                false
            )
        ) {
            for (entry in oldSharedPreferences.all.entries) {
                val key = entry.key
                val value: Any? = entry.value
                newSharedPreferences.set(key, value)
            }
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
