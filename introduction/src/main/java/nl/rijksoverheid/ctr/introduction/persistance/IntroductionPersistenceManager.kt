package nl.rijksoverheid.ctr.introduction.persistance

import android.content.SharedPreferences

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class IntroductionPersistenceManager(
    private val sharedPreferences: SharedPreferences
) {

    companion object {
        const val INTRODUCTION_FINISHED = "INTRODUCTION_FINISHED"
        const val NEW_TERMS_SEEN = "NEW_TERMS_SEEN_[VERSION]"
        const val NEW_FEATURES_SEEN = "NEW_FEATURES_SEEN_[VERSION]"
    }

    fun saveIntroductionFinished() {
        sharedPreferences.edit().putBoolean(INTRODUCTION_FINISHED, true).apply()
    }

    fun getIntroductionFinished(): Boolean {
        return sharedPreferences.getBoolean(INTRODUCTION_FINISHED, false)
    }

    fun getNewTermsSeen(version: Int): Boolean {
        return getNewSeen(NEW_TERMS_SEEN, version)
    }

    fun saveNewTermsSeen(version: Int) {
        saveNewSeen(NEW_TERMS_SEEN, version)
    }

    fun getNewFeaturesSeen(version: Int): Boolean {
        return getNewSeen(NEW_FEATURES_SEEN, version)
    }

    fun saveNewFeaturesSeen(version: Int) {
        saveNewSeen(NEW_FEATURES_SEEN, version)
    }

    private fun getNewSeen(type: String, version: Int): Boolean {
        return sharedPreferences.getBoolean(
            type.replace("[VERSION]", version.toString()),
            false
        )
    }

    private fun saveNewSeen(type: String, version: Int) {
        sharedPreferences.edit()
            .putBoolean(
                type.replace(
                    "[VERSION]",
                    version.toString(),
                ), true
            ).apply()
    }
}
