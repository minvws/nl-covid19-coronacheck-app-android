/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig.usecases

import android.os.SystemClock
import nl.rijksoverheid.ctr.appconfig.api.model.HolderConfig
import nl.rijksoverheid.ctr.appconfig.persistence.ClockDeviationPersistenceManager
import java.time.Clock
import kotlin.math.abs

interface ClockDeviationUseCase {
    fun store(serverResponseTimestamp: Long, localReceivedTimestamp: Long)
    fun calculateDeviationState(): Boolean
    fun retrieveDeviationState(): Boolean
}

const val SECOND_IN_MS = 1000

class ClockDeviationUseCaseImpl(
    private val clockDeviationPersistenceManager: ClockDeviationPersistenceManager,
    private val clock: Clock,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase
) : ClockDeviationUseCase {

    private var localServerResponseTimeStamp: Long = 0
    private var localResponseReceivedTimeStamp: Long = 0
    private var localUptimeAtResponse: Long = 0


    override fun store(serverResponseTimestamp: Long, localReceivedTimestamp: Long) {
        this.localServerResponseTimeStamp = serverResponseTimestamp
        this.localResponseReceivedTimeStamp = localReceivedTimestamp
        this.localUptimeAtResponse = SystemClock.elapsedRealtime()
    }

    override fun calculateDeviationState(): Boolean {
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
        clockDeviationPersistenceManager.saveDeviationState(hasDeviation)
        return hasDeviation
    }

    override fun retrieveDeviationState(): Boolean {
        return clockDeviationPersistenceManager.getHasDeviationState()
    }
}