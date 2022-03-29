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
        const val SETUP_FINISHED = "SETUP_FINISHED"
        const val INTRODUCTION_FINISHED = "INTRODUCTION_FINISHED"
    }

    fun saveSetupFinished() {
        sharedPreferences.edit().putBoolean(SETUP_FINISHED, true).apply()
    }

    fun getSetupFinished(): Boolean {
        return sharedPreferences.getBoolean(SETUP_FINISHED, false)
    }

    fun saveIntroductionFinished() {
        sharedPreferences.edit().putBoolean(INTRODUCTION_FINISHED, true).apply()
    }

    fun getIntroductionFinished(): Boolean {
        return sharedPreferences.getBoolean(INTRODUCTION_FINISHED, false)
    }
}
