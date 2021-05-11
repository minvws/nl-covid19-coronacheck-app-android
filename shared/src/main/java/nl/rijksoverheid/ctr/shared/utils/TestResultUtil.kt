package nl.rijksoverheid.ctr.shared.utils

import java.time.Clock
import java.time.OffsetDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface TestResultUtil {
    fun isValid(
        sampleDate: OffsetDateTime,
        validitySeconds: Long
    ): Boolean
}

class TestResultUtilImpl(private val clock: Clock) : TestResultUtil {

    override fun isValid(
        sampleDate: OffsetDateTime,
        validitySeconds: Long
    ): Boolean {
        return OffsetDateTime.now(clock).isBefore(sampleDate.plusSeconds(validitySeconds))
    }
}
