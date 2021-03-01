package nl.rijksoverheid.ctr.shared.ext

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

// TODO: Other country support
fun OffsetDateTime.formatDate(): String =
    DateTimeFormatter.ofPattern(
        "d MMMM yyyy"
    ).format(this)
