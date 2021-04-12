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
    }

    fun saveIntroductionFinished() {
        sharedPreferences.edit().putBoolean(INTRODUCTION_FINISHED, true).apply()
    }

    fun getIntroductionFinished(): Boolean {
        return sharedPreferences.getBoolean(INTRODUCTION_FINISHED, false)
    }

    fun getNewTermsSeen(version: Int): Boolean {
        return sharedPreferences.getBoolean(
            NEW_TERMS_SEEN.replace("[VERSION]", version.toString()),
            false
        )
    }

    fun saveNewTermsSeen(version: Int) {
        sharedPreferences.edit()
            .putBoolean(
                NEW_TERMS_SEEN.replace(
                    "[VERSION]",
                    version.toString(),
                ), true
            ).apply()
    }
}
