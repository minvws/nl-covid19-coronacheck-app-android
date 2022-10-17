package nl.rijksoverheid.ctr.persistence.database.entities

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
sealed class RemovedEventReason {

    companion object {
        const val BLOCKED = "blocked"
        const val FUZZY_MATCHED = "fuzzy_matched"
    }

    object Blocked : RemovedEventReason()
    object FuzzyMatched : RemovedEventReason()
}
