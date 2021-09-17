package nl.rijksoverheid.ctr.holder.persistence.database.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@Entity(
    tableName = "green_card",
    foreignKeys = [ForeignKey(
        entity = WalletEntity::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("wallet_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
@Parcelize
data class GreenCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "wallet_id", index = true) val walletId: Int,
    val type: GreenCardType,
): Parcelable
