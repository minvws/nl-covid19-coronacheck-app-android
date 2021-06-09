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
sealed class GreenCardType : Parcelable {

    companion object {
        const val TYPE_DOMESTIC = "domestic"
        const val TYPE_EU = "eu"
    }

    @Parcelize
    object Domestic : GreenCardType(), Parcelable

    @Parcelize
    object Eu : GreenCardType(), Parcelable
}
