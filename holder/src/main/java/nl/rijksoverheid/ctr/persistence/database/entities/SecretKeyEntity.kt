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

@Entity(
    tableName = "secret_key",
    foreignKeys = [ForeignKey(
        entity = GreenCardEntity::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("green_card_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class SecretKeyEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "green_card_id") val greenCardId: Int,
    @ColumnInfo(name = "secret") val secretKey: String
)