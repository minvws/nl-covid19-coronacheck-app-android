/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig.persistence

import android.content.SharedPreferences

interface ClockDeviationPersistenceManager {
    fun saveDeviationState(hasDeviation : Boolean)
    fun getHasDeviationState() : Boolean
}

class ClockDeviationPersistenceManagerImpl(private val sharedPreferences: SharedPreferences) :
    ClockDeviationPersistenceManager {

    companion object {
        const val CLOCK_DEVIATION_HAS_DEVIATION = "CLOCK_DEVIATION_HAS_DEVIATION"
    }

    override fun saveDeviationState(hasDeviation: Boolean) {
        sharedPreferences.edit().putBoolean(CLOCK_DEVIATION_HAS_DEVIATION, hasDeviation).apply()
    }

    override fun getHasDeviationState(): Boolean {
        return sharedPreferences.getBoolean(CLOCK_DEVIATION_HAS_DEVIATION, false)
    }

}