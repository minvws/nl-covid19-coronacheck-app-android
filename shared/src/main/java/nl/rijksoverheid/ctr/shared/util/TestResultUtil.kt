package nl.rijksoverheid.ctr.shared.util

import java.time.Clock
import java.time.OffsetDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class TestResultUtil(private val clock: Clock) {

    fun isValid(
        sampleDate: OffsetDateTime,
        validitySeconds: Long,
        isPaperProof: String
    ): Boolean {
        return if (isPaperProof == "1") {
            true
        } else {
            OffsetDateTime.now(clock).isBefore(sampleDate.plusSeconds(validitySeconds))
        }
    }
}
