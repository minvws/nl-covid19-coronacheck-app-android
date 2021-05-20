package nl.rijksoverheid.ctr.holder.persistence.database.entities

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
sealed class GreenCardType {

    companion object {
        const val TYPE_DOMESTIC = "domestic"
        const val TYPE_EU = "eu"
    }

    object Domestic : GreenCardType()
    object Eu : GreenCardType()
}
