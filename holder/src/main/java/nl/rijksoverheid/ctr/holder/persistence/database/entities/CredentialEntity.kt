package nl.rijksoverheid.ctr.holder.persistence.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.OffsetDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@Entity(
    tableName = "credential",
    foreignKeys = [ForeignKey(
        entity = GreenCardEntity::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("green_card_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class CredentialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "green_card_id") val greenCardId: Long,
    val data: String,
    val credentialVersion: Int,
    val validFrom: OffsetDateTime
)
