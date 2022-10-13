package nl.rijksoverheid.ctr.holder.fuzzy_matching

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@Parcelize
data class MatchingBlobIds(val ids: ArrayList<ArrayList<Int>>) : Parcelable {
    companion object {
        fun fromList(ids: List<List<Int>>): MatchingBlobIds {
            return MatchingBlobIds(ArrayList(ids.map { ArrayList(it) }))
        }
    }
}
