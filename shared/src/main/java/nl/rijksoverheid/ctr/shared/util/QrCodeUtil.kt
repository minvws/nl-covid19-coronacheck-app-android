package nl.rijksoverheid.ctr.shared.util

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
class QrCodeUtil {

    companion object {
        val VALID_FOR_SECONDS = TimeUnit.MINUTES.toSeconds(3)
    }

    fun isValid(currentDate: OffsetDateTime, creationDate: OffsetDateTime): Boolean {
        return ChronoUnit.SECONDS.between(creationDate, currentDate) <= VALID_FOR_SECONDS
    }
}
