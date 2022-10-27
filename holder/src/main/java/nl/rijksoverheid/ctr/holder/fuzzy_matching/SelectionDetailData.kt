package nl.rijksoverheid.ctr.holder.fuzzy_matching

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
data class SelectionDetailData(
    val type: String,
    val providerIdentifiers: List<String>,
    val eventDate: String
)
