package nl.rijksoverheid.ctr.holder.ui.myoverview.utils

import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import java.time.Clock
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

interface MyOverviewGreenCardAdapterUtil {
    sealed class ExpireCountDown {
        data class Show(val hoursLeft: Long, val minutesLeft: Long) : ExpireCountDown()
        object Hide : ExpireCountDown()
    }

    fun getExpireCountdownText(
        expireDate: OffsetDateTime,
        type: OriginType
    ): ExpireCountDown
}

class MyOverviewGreenCardAdapterUtilImpl(private val clock: Clock) : MyOverviewGreenCardAdapterUtil {

    private val minutesInSeconds = 60
    private val hoursInSeconds = 60 * 60

    override fun getExpireCountdownText(
        expireDate: OffsetDateTime,
        type: OriginType
    ): MyOverviewGreenCardAdapterUtil.ExpireCountDown {
        val hoursBetweenExpiration =
            ChronoUnit.HOURS.between(OffsetDateTime.now(clock), expireDate)
        return if (hoursBetweenExpiration >= getExpiryForType(type)) {
            MyOverviewGreenCardAdapterUtil.ExpireCountDown.Hide
        } else {
            var diff =
                expireDate.toEpochSecond() - OffsetDateTime.now(clock)
                    .toEpochSecond()
            val hoursUntilFinish = diff / hoursInSeconds
            diff %= hoursInSeconds
            val minutesUntilFinish = (diff / minutesInSeconds).coerceAtLeast(1)
            MyOverviewGreenCardAdapterUtil.ExpireCountDown.Show(
                hoursLeft = hoursUntilFinish,
                minutesLeft = minutesUntilFinish
            )
        }
    }

    private fun getExpiryForType(type: OriginType): Int {
        return if (type == OriginType.Test) 6 else 24
    }
}
