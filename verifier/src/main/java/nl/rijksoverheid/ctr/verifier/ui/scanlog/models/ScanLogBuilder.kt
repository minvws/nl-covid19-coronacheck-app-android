package nl.rijksoverheid.ctr.verifier.ui.scanlog.models

import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import java.time.OffsetDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ScanLogBuilder() {
    var policy: VerificationPolicy? = null
    var count: Int? = null
    var skew: Boolean = false
    var from: OffsetDateTime? = null
    var to: OffsetDateTime? = null

    fun build(): ScanLog {
        val policy: VerificationPolicy = policy ?: error("Should not be null")
        val count: Int = count ?: error("Should not be null")
        val skew: Boolean = skew
        val from: OffsetDateTime = from ?: error("Should not be null")
        val to: OffsetDateTime = to ?: error("Should not be null")

        return ScanLog(
            policy = policy,
            count = count,
            skew = skew,
            from = from,
            to = to
        )
    }
}