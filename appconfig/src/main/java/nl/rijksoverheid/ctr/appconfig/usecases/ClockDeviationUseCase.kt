/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig.usecases

import android.os.SystemClock
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import nl.rijksoverheid.ctr.appconfig.models.ServerTime
import nl.rijksoverheid.ctr.shared.livedata.Event
import java.time.Clock
import java.util.concurrent.TimeUnit
import kotlin.math.abs

abstract class ClockDeviationUseCase {
    val serverTimeSyncedLiveData: LiveData<Event<Unit>> = MutableLiveData()

    abstract fun store(serverTime: ServerTime)
    abstract fun hasDeviation(): Boolean
    abstract fun calculateServerTimeOffsetMillis(): Long
}

class ClockDeviationUseCaseImpl(
    private val clock: Clock,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase
) : ClockDeviationUseCase() {

    private var cachedServerTime: ServerTime = ServerTime.NotAvailable

    override fun store(serverTime: ServerTime) {
        this.cachedServerTime = serverTime
        (serverTimeSyncedLiveData as MutableLiveData).postValue(Event(Unit))
    }

    override fun hasDeviation(): Boolean {
        val thresholdInSeconds = cachedAppConfigUseCase.getCachedAppConfig().clockDeviationThresholdSeconds
        val serverTimeOffsetSeconds = TimeUnit.MILLISECONDS.toSeconds(abs(calculateServerTimeOffsetMillis()))
        return serverTimeOffsetSeconds >= thresholdInSeconds
    }

    /**
     * Gets the calculated offset from server time
     * A negative offset means the device clock is behind, a positive offset means the device is ahead
     * @return the offset in millis
     */
    override fun calculateServerTimeOffsetMillis(): Long {
        when (val serverInfo = cachedServerTime) {
            is ServerTime.NotAvailable -> {
                return 0L
            }
            is ServerTime.Available -> {
                val currentUptime = SystemClock.elapsedRealtime()
                val currentMillis = clock.instant().toEpochMilli()

                val systemStartMillis = currentMillis - currentUptime
                val responseStartMillis = serverInfo.localTimeMillis - serverInfo.uptime

                val responseTimeDelta = serverInfo.localTimeMillis - serverInfo.serverTimeMillis
                val systemUptimeDelta = systemStartMillis - responseStartMillis

                return systemUptimeDelta + responseTimeDelta
            }
        }
    }
}