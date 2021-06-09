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
sealed class OriginType : Parcelable {

    companion object {
        const val TYPE_VACCINATION = "vaccination"
        const val TYPE_RECOVERY = "recovery"
        const val TYPE_TEST = "test"
    }

    @Parcelize
    object Vaccination : OriginType(), Parcelable

    @Parcelize
    object Recovery : OriginType(), Parcelable

    @Parcelize
    object Test : OriginType(), Parcelable
}
