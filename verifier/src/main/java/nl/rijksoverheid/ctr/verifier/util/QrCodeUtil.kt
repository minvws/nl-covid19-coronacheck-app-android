package nl.rijksoverheid.ctr.verifier.util

import nl.rijksoverheid.ctr.shared.QrCodeConstants
import java.time.Clock
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface QrCodeUtil {
    fun isValid(creationDate: OffsetDateTime, isPaperProof: String): Boolean
}

class QrCodeUtilImpl(private val clock: Clock) : QrCodeUtil {

    override fun isValid(creationDate: OffsetDateTime, isPaperProof: String): Boolean {
        return if (isPaperProof == "1") {
            true
        } else {
            abs(
                ChronoUnit.SECONDS.between(
                    creationDate,
                    OffsetDateTime.now(clock)
                )
            ) <= QrCodeConstants.VALID_FOR_SECONDS
        }
    }
}
