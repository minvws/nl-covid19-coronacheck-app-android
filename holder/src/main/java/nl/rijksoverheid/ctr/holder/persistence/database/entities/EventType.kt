package nl.rijksoverheid.ctr.holder.persistence.database.entities

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
sealed class EventType : DatabaseType {

    companion object {
        const val TYPE_VACCINATION = "vaccination"
        const val TYPE_TEST = "test"
        const val TYPE_RECOVERY = "recovery"
    }

    object Vaccination : EventType()
    object Test : EventType()
    object Recovery : EventType()
}
