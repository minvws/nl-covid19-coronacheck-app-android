package nl.rijksoverheid.ctr.persistence.database.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

/**
 * Types of origins that can be returned by the backend or the database
 */
sealed class OriginType(val order: Int) : Parcelable {

    companion object {
        const val TYPE_VACCINATION = "vaccination"
        const val TYPE_RECOVERY = "recovery"
        const val TYPE_TEST = "test"

        fun fromTypeString(typeString: String): OriginType {
            return when (typeString) {
                TYPE_VACCINATION -> Vaccination
                TYPE_RECOVERY -> Recovery
                TYPE_TEST -> Test
                else -> throw IllegalStateException("Type not known")
            }
        }
    }

    @Parcelize
    object Vaccination : OriginType(1)

    @Parcelize
    object Recovery : OriginType(2)

    @Parcelize
    object Test : OriginType(4)

    fun getTypeString(): String {
        return when (this) {
            is Vaccination -> TYPE_VACCINATION
            is Recovery -> TYPE_RECOVERY
            is Test -> TYPE_TEST
        }
    }
}
