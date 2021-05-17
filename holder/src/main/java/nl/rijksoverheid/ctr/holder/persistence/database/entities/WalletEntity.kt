package nl.rijksoverheid.ctr.holder.persistence.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@Entity(tableName = "wallet")
data class WalletEntity(
    @PrimaryKey val id: Int,
    val label: String
)
