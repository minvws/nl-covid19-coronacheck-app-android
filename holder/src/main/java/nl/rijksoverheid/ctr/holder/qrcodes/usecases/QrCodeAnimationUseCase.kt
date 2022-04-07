package nl.rijksoverheid.ctr.holder.qrcodes.usecases

import nl.rijksoverheid.ctr.holder.qrcodes.models.QrCodeAnimation
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import java.time.Clock
import java.util.*


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface QrCodeAnimationUseCase {
    fun get(greenCardType: GreenCardType): QrCodeAnimation
}

class QrCodeAnimationUseCaseImpl(
    private val clock: Clock
) : QrCodeAnimationUseCase {

    override fun get(greenCardType: GreenCardType): QrCodeAnimation {
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
                QrCodeAnimation.DomesticSummer
            } else {
                QrCodeAnimation.DomesticWinter
            }
            GreenCardType.Eu -> if (isSummer) {
                QrCodeAnimation.EuSummer
            } else {
                QrCodeAnimation.EuWinter
            }
        }
    }

    companion object {
        private const val startDay = 21
        private const val endDay = 20
    }
}