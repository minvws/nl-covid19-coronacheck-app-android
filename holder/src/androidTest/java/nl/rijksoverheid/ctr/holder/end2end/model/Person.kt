/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end.model

import java.time.LocalDate

data class Person(
    val bsn: String = "999991772",
    val name: String = "van Geer, Corrie",
    val birthDate: LocalDate = LocalDate.of(1960, 1, 1)
)
