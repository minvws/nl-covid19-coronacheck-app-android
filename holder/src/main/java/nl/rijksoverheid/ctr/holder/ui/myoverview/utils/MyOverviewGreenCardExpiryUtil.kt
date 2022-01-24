package nl.rijksoverheid.ctr.holder.ui.myoverview.utils

import android.content.Context
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginEntity
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

interface MyOverviewGreenCardExpiryUtil {
    sealed class ExpireCountDown {
        data class Show(val hoursLeft: Long, val minutesLeft: Long) : ExpireCountDown()
        object Hide : ExpireCountDown()
    }

    fun getExpireCountdown(
        expireDate: OffsetDateTime,
        type: OriginType
    ): ExpireCountDown

    fun getExpiryText(result: ExpireCountDown.Show): String

    /**
     * Get the last origin that's valid if it's the only valid one.
     *
     * @param[origins] origins list to get the last one valid from
     * @return last origin that's valid or null when none or more are valid
     */
    fun getLastValidOrigin(origins: List<OriginEntity>): OriginEntity?
}

class MyOverviewGreenCardExpiryUtilImpl(
    private val clock: Clock,
    private val context: Context
) : MyOverviewGreenCardExpiryUtil {

    private val minutesInSeconds = 60
    private val hoursInSeconds = 60 * 60

    override fun getExpireCountdown(
        expireDate: OffsetDateTime,
        type: OriginType
    ): MyOverviewGreenCardExpiryUtil.ExpireCountDown {
        val hoursBetweenExpiration =
            ChronoUnit.HOURS.between(OffsetDateTime.now(clock), expireDate)
        return if (hoursBetweenExpiration >= getExpiryHoursForType(type)) {
            MyOverviewGreenCardExpiryUtil.ExpireCountDown.Hide
        } else {
            var diff =
                expireDate.toEpochSecond() - OffsetDateTime.now(clock)
                    .toEpochSecond()
            val hoursUntilFinish = diff / hoursInSeconds
            diff %= hoursInSeconds
            val minutesUntilFinish = (diff / minutesInSeconds).coerceAtLeast(1)
            MyOverviewGreenCardExpiryUtil.ExpireCountDown.Show(
                hoursLeft = hoursUntilFinish,
                minutesLeft = minutesUntilFinish
            )
        }
    }

    private fun getExpiryHoursForType(type: OriginType): Int {
        return if (type == OriginType.Test) 6 else 24
    }

    override fun getExpiryText(
        result: MyOverviewGreenCardExpiryUtil.ExpireCountDown.Show
    ): String {
        val hoursLeft = result.hoursLeft.toInt()
        val minutesLeft = result.minutesLeft.toInt()
        return if (hoursLeft == 0) {
            context.getString(
                R.string.my_overview_test_result_expires_in_minutes,
                "$minutesLeft ${context.resources.getQuantityString(R.plurals.my_overview_test_result_expires_minutes, minutesLeft)}"
            )
        } else {
            context.getString(
                R.string.my_overview_test_result_expires_in_hours_minutes,
                "$hoursLeft ${context.resources.getQuantityString(R.plurals.my_overview_test_result_expires_hours, hoursLeft)}",
                "$minutesLeft ${context.resources.getQuantityString(R.plurals.my_overview_test_result_expires_minutes, minutesLeft)}",
            )
        }
    }

    override fun getLastValidOrigin(origins: List<OriginEntity>): OriginEntity? {
        return origins.filter { it.expirationTime > OffsetDateTime.now(clock) }
            .takeIf { it.size == 1 }
            ?.firstOrNull()
    }
}
