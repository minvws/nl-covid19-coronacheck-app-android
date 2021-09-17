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
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val data: ByteArray,
    val credentialVersion: Int,
    val validFrom: OffsetDateTime,
    val expirationTime: OffsetDateTime
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CredentialEntity

        if (id != other.id) return false
        if (greenCardId != other.greenCardId) return false
        if (!data.contentEquals(other.data)) return false
        if (credentialVersion != other.credentialVersion) return false
        if (validFrom != other.validFrom) return false
        if (expirationTime != other.expirationTime) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + greenCardId.hashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + credentialVersion
        result = 31 * result + validFrom.hashCode()
        result = 31 * result + expirationTime.hashCode()
        return result
    }
}