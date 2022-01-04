package nl.rijksoverheid.ctr.design.ext

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
fun Instant.toOffsetDateTimeUtc(): OffsetDateTime {
    return OffsetDateTime.ofInstant(this, ZoneId.of("UTC"))
}