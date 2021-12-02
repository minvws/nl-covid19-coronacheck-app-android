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
data class ScanLog(
    val policy: VerificationPolicy,
    val count: Int,
    val skew: Boolean,
    val from: OffsetDateTime,
    val to: OffsetDateTime
)