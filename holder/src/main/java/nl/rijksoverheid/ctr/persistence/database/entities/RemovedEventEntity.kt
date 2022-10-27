/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.persistence.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.OffsetDateTime

@Entity(
    tableName = "removed_event",
    foreignKeys = [ForeignKey(
        entity = WalletEntity::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("wallet_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class RemovedEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "wallet_id", index = true) val walletId: Int,
    val type: String,
    @ColumnInfo(name = "event_time") val eventTime: OffsetDateTime?,
    @ColumnInfo(defaultValue = "blocked") val reason: RemovedEventReason
)
