package nl.rijksoverheid.ctr.holder.persistence.database.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
sealed class OriginType(val order: Int) : Parcelable {

    companion object {
        const val TYPE_VACCINATION = "vaccination"
        const val TYPE_RECOVERY = "recovery"
        const val TYPE_TEST = "test"
    }

    @Parcelize
    object Vaccination : OriginType(1), Parcelable

    @Parcelize
    object Recovery : OriginType(2), Parcelable

    @Parcelize
    object Test : OriginType(3), Parcelable

    fun getTypeString(): String {
        return when (this) {
            is Vaccination -> TYPE_VACCINATION
            is Recovery -> TYPE_RECOVERY
            is Test -> TYPE_TEST
        }
    }
}
