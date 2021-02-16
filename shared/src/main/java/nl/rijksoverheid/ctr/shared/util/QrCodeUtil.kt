package nl.rijksoverheid.ctr.shared.util

import java.time.Clock
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class QrCodeUtil(private val clock: Clock) {

    companion object {
        val VALID_FOR_SECONDS = TimeUnit.MINUTES.toSeconds(3)
    }

    fun isValid(creationDate: OffsetDateTime): Boolean {
        return ChronoUnit.SECONDS.between(
            creationDate,
            OffsetDateTime.now(clock).plusSeconds(VALID_FOR_SECONDS)
        ) >= 0
    }
}
