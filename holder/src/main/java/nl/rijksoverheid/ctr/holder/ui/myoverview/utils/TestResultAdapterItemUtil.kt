package nl.rijksoverheid.ctr.holder.ui.myoverview.utils

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

interface TestResultAdapterItemUtil {
    sealed class ExpireCountDown {
        data class Show(val hoursLeft: Long, val minutesLeft: Long) : ExpireCountDown()
        object Hide : ExpireCountDown()
    }

    fun getExpireCountdownText(
        expireDate: OffsetDateTime
    ): ExpireCountDown
}

class TestResultAdapterItemUtilImpl(private val clock: Clock) : TestResultAdapterItemUtil {

    private val minutesInSeconds = 60
    private val hoursInSeconds = 60 * 60

    /**
     * Get if we need to show a countdown string in [nl.rijksoverheid.ctr.holder.ui.myoverview.items.MyOverviewTestResultAdapterItem]]
     */
    override fun getExpireCountdownText(
        expireDate: OffsetDateTime
    ): TestResultAdapterItemUtil.ExpireCountDown {
        val hoursBetweenExpiration =
            ChronoUnit.HOURS.between(OffsetDateTime.now(clock), expireDate)
        return if (hoursBetweenExpiration > 5) {
            TestResultAdapterItemUtil.ExpireCountDown.Hide
        } else {
            var diff =
                expireDate.toEpochSecond() - OffsetDateTime.now(clock)
                    .toEpochSecond()
            val hoursUntilFinish = diff / hoursInSeconds
            diff %= hoursInSeconds
            val minutesUntilFinish = (diff / minutesInSeconds).coerceAtLeast(1)
            TestResultAdapterItemUtil.ExpireCountDown.Show(
                hoursLeft = hoursUntilFinish,
                minutesLeft = minutesUntilFinish
            )
        }
    }

}
