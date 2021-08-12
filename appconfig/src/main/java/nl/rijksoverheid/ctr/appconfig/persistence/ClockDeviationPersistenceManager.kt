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
    fun getLastServerResponseTimestamp() : Long
    fun getLastLocalResponseReceivedTimestamp() : Long
    fun getSystemUptimeTimestamp() : Long
    fun saveDeviationState(hasDeviation : Boolean)
    fun getHasDeviationState() : Boolean
    fun saveTimestamps(serverResponseTimeStamp : Long, localResponseReceivedTimeStamp : Long, localUptime : Long)
}

class ClockDeviationPersistenceManagerImpl(private val sharedPreferences: SharedPreferences) :
    ClockDeviationPersistenceManager {

    companion object {
        const val CLOCK_DEVIATION_LAST_SERVER_RESPONSE = "CLOCK_DEVIATION_LAST_SERVER_RESPONSE"
        const val CLOCK_DEVIATION_LAST_LOCAL_RECEIVED = "CLOCK_DEVIATION_LAST_LOCAL_RECEIVED"
        const val CLOCK_DEVIATION_SYSTEM_UPTIME = "CLOCK_DEVIATION_SYSTEM_UPTIME"
        const val CLOCK_DEVIATION_HAS_DEVIATION = "CLOCK_DEVIATION_HAS_DEVIATION"
    }

    override fun getLastServerResponseTimestamp(): Long {
        return sharedPreferences.getLong(CLOCK_DEVIATION_LAST_SERVER_RESPONSE, 0)
    }

    override fun getLastLocalResponseReceivedTimestamp(): Long {
        return sharedPreferences.getLong(CLOCK_DEVIATION_LAST_LOCAL_RECEIVED, 0)
    }

    override fun getSystemUptimeTimestamp(): Long {
        return sharedPreferences.getLong(CLOCK_DEVIATION_SYSTEM_UPTIME, 0)
    }

    override fun saveDeviationState(hasDeviation: Boolean) {
        sharedPreferences.edit().putBoolean(CLOCK_DEVIATION_HAS_DEVIATION, hasDeviation).apply()
    }

    override fun getHasDeviationState(): Boolean {
        return sharedPreferences.getBoolean(CLOCK_DEVIATION_HAS_DEVIATION, false)
    }

    override fun saveTimestamps(
        serverResponseTimeStamp: Long,
        localResponseReceivedTimeStamp: Long,
        localUptime: Long
    ) {
        sharedPreferences.edit().
        putLong(CLOCK_DEVIATION_LAST_SERVER_RESPONSE, serverResponseTimeStamp).
        putLong(CLOCK_DEVIATION_LAST_LOCAL_RECEIVED, localResponseReceivedTimeStamp).
        putLong(CLOCK_DEVIATION_SYSTEM_UPTIME, localUptime).apply()
    }
}