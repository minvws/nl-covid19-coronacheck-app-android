package nl.rijksoverheid.ctr.holder.qrcodes.usecases

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class QrCodeAnimationUseCaseTests {

    @Test
    fun `test summer`() {
        val summerStart = Clock.fixed(Instant.parse("2023-03-21T00:00:00.00Z"), ZoneId.of("UTC"))
        val calendar = Calendar.getInstance().apply {
            timeInMillis = summerStart.millis()
        }

        // loop through 21 Mar to 20 Dec incl
        for (i in 1..274) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val clock = Clock.fixed(calendar.toInstant(), ZoneId.of("UTC"))
            val animationUseCase = QrCodeAnimationUseCaseImpl(clock)
            assertEquals(
                R.raw.summer_domestic,
                animationUseCase.get(GreenCardType.Domestic).animationResource
            )
            assertEquals(
                R.raw.summer_international,
                animationUseCase.get(GreenCardType.Eu).animationResource
            )
        }
    }

    @Test
    fun `test winter`() {
        val winterStart = Clock.fixed(Instant.parse("2022-12-21T00:00:00.00Z"), ZoneId.of("UTC"))
        val calendar = Calendar.getInstance().apply {
            timeInMillis = winterStart.millis()
        }

        // loop through 21 Dec to 20 Mar incl
        for (i in 1..89) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val clock = Clock.fixed(calendar.toInstant(), ZoneId.of("UTC"))
            val animationUseCase = QrCodeAnimationUseCaseImpl(clock)
            assertEquals(
                R.raw.winter_domestic,
                animationUseCase.get(GreenCardType.Domestic).animationResource
            )
            assertEquals(
                R.raw.winter_international,
                animationUseCase.get(GreenCardType.Eu).animationResource
            )
        }
    }
}
