/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard.util

import java.time.Clock
import java.time.OffsetDateTime
import nl.rijksoverheid.ctr.persistence.database.entities.OriginEntity

interface OriginUtil {
    fun getOriginState(origins: List<OriginEntity>): List<OriginState>

    fun isValidWithinRenewalThreshold(credentialRenewalDays: Long, origin: OriginEntity): Boolean
}

class OriginUtilImpl(private val clock: Clock) : OriginUtil {

    companion object {
        private const val PRESENT_SUBTITLE_WHEN_LESS_THEN_YEARS = 3
    }

    override fun getOriginState(origins: List<OriginEntity>): List<OriginState> {
        return origins.map { origin ->
            when {
                origin.expirationTime.isBefore(OffsetDateTime.now(clock)) -> {
                    OriginState.Expired(origin)
                }
                origin.validFrom.isAfter(OffsetDateTime.now(clock)) -> {
                    OriginState.Future(origin)
                }
                else -> {
                    OriginState.Valid(origin)
                }
            }
        }
    }

    override fun isValidWithinRenewalThreshold(
        credentialRenewalDays: Long,
        origin: OriginEntity
    ): Boolean {
        val now = OffsetDateTime.now(clock)
        val thresholdEndDate = now.plusDays(credentialRenewalDays)
        return origin.validFrom < thresholdEndDate && origin.expirationTime > now
    }
}

sealed class OriginState(open val origin: OriginEntity) {
    data class Valid(override val origin: OriginEntity) : OriginState(origin)
    data class Future(override val origin: OriginEntity) : OriginState(origin)
    data class Expired(override val origin: OriginEntity) : OriginState(origin)
}
