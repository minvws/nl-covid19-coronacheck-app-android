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
import nl.rijksoverheid.ctr.appconfig.api.model.HolderConfig
import nl.rijksoverheid.ctr.shared.livedata.Event
import java.time.Clock
import kotlin.math.abs

abstract class ClockDeviationUseCase {
    val serverTimeSyncedLiveData: LiveData<Event<Unit>> = MutableLiveData()

    abstract fun store(serverResponseTimestamp: Long, localReceivedTimestamp: Long)
    abstract fun hasDeviation(): Boolean
}

const val SECOND_IN_MS = 1000

class ClockDeviationUseCaseImpl(
    private val clock: Clock,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase
) : ClockDeviationUseCase() {

    private var localServerResponseTimeStamp: Long = 0
    private var localResponseReceivedTimeStamp: Long = 0
    private var localUptimeAtResponse: Long = 0

    override fun store(serverResponseTimestamp: Long, localReceivedTimestamp: Long) {
        this.localServerResponseTimeStamp = serverResponseTimestamp
        this.localResponseReceivedTimeStamp = localReceivedTimestamp
        this.localUptimeAtResponse = SystemClock.elapsedRealtime()
        (serverTimeSyncedLiveData as MutableLiveData).postValue(Event(Unit))
    }

    override fun hasDeviation(): Boolean {
        if (localResponseReceivedTimeStamp == 0L && localServerResponseTimeStamp == 0L) {
            return false
        }

        val currentLocalUptime = SystemClock.elapsedRealtime()
        val localTime = clock.instant().toEpochMilli()
        val currentSystemStartDatetime = localTime - currentLocalUptime
        val responseSystemStartDatetime = localResponseReceivedTimeStamp - localUptimeAtResponse
        val responseTimeDelta = localResponseReceivedTimeStamp - localServerResponseTimeStamp
        val systemUptimeDelta = currentSystemStartDatetime - responseSystemStartDatetime
        val thresholdInSeconds =
            (cachedAppConfigUseCase.getCachedAppConfig() as HolderConfig).holderClockDeviationThresholdSeconds

        val hasDeviation =
            (abs(systemUptimeDelta + responseTimeDelta) / SECOND_IN_MS) >= thresholdInSeconds
        return hasDeviation
    }
}