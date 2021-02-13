package nl.rijksoverheid.ctr.shared.util

import java.time.OffsetDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class TestResultUtil {

    fun isValid(
        currentDate: OffsetDateTime,
        sampleDate: OffsetDateTime,
        validitySeconds: Long
    ): Boolean {
        return currentDate.isBefore(sampleDate.plusSeconds(validitySeconds))
    }
}
