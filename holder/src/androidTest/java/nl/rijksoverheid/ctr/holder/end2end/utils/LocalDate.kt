/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun LocalDate.short(): String {
    return this.format(DateTimeFormatter.ISO_LOCAL_DATE)
}

fun LocalDate.written(): String {
    return this.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
}

fun LocalDate.writtenWithoutYear(): String {
    return this.format(DateTimeFormatter.ofPattern("d MMMM"))
}

fun LocalDate.recently(): String {
    return this.format(DateTimeFormatter.ofPattern("EEEE d MMMM"))
}

fun LocalDate.dutch(): String {
    return this.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
}
