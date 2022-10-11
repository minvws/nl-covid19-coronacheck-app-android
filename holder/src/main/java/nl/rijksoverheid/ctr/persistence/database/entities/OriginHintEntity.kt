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
    tableName = "origin_hint",
    foreignKeys = [ForeignKey(
        entity = OriginEntity::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("origin_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class OriginHintEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "origin_id", index = true) val originId: Long,
    @ColumnInfo(index = true) val hint: String
)
