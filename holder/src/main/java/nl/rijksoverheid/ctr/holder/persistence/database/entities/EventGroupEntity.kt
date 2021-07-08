package nl.rijksoverheid.ctr.holder.persistence.database.entities

import androidx.room.*
import java.time.LocalDate
import java.time.OffsetDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@Entity(
    indices = [Index(value = ["provider_identifier", "type"], unique = true)],
    tableName = "event_group",
    foreignKeys = [ForeignKey(
        entity = WalletEntity::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("wallet_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class EventGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "wallet_id", index = true) val walletId: Int,
    @ColumnInfo(name = "provider_identifier") val providerIdentifier: String,
    val type: OriginType,
    val maxIssuedAt: OffsetDateTime,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val jsonData: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EventGroupEntity

        if (id != other.id) return false
        if (walletId != other.walletId) return false
        if (providerIdentifier != other.providerIdentifier) return false
        if (type != other.type) return false
        if (maxIssuedAt != other.maxIssuedAt) return false
        if (!jsonData.contentEquals(other.jsonData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + walletId
        result = 31 * result + providerIdentifier.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + maxIssuedAt.hashCode()
        result = 31 * result + jsonData.contentHashCode()
        return result
    }
}
