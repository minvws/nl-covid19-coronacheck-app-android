package nl.rijksoverheid.ctr.holder.qrcodes.models

import androidx.annotation.RawRes
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import java.time.Clock
import java.util.*

/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */
sealed class QrCodeAnimation(@RawRes val animationResource: Int) {
    object DomesticWinter : QrCodeAnimation(R.raw.winter_domestic)
    object EuWinter : QrCodeAnimation(R.raw.winter_international)
    object DomesticSummer : QrCodeAnimation(R.raw.summer_domestic)
    object EuSummer : QrCodeAnimation(R.raw.summer_international)

    companion object {
        private const val startDay = 21
        private const val endDay = 20

        fun getCurrent(clock: Clock, greenCardType: GreenCardType): QrCodeAnimation {
            val now = Calendar.getInstance().apply {
                timeInMillis = clock.millis()
            }
            val dayOfMonth = now.get(Calendar.DAY_OF_MONTH)
            val month = now.get(Calendar.MONTH)
            val isSummer =
                (dayOfMonth >= startDay && month == Calendar.MARCH) ||
                        month in (Calendar.APRIL..Calendar.NOVEMBER) ||
                        (dayOfMonth <= endDay && month == Calendar.DECEMBER)

            return when (greenCardType) {
                GreenCardType.Domestic -> if (isSummer) {
                    DomesticSummer
                } else {
                    DomesticWinter
                }
                GreenCardType.Eu -> if (isSummer) {
                    EuSummer
                } else {
                    EuWinter
                }
            }
        }
    }
}