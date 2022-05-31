/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

object AndroidTestUtils {

    fun getOffsetDateTime(dateString: String): OffsetDateTime =
        OffsetDateTime.ofInstant(Instant.parse(dateString), ZoneId.of("UTC"))
}