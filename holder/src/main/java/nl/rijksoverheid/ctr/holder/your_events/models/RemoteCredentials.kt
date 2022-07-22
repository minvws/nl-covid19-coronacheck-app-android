/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.your_events.models

import com.squareup.moshi.JsonClass
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import java.time.OffsetDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@JsonClass(generateAdapter = true)
data class RemoteGreenCards(
    val domesticGreencard: DomesticGreenCard?,
    val euGreencards: List<EuGreenCard>?,
    val blobExpireDates: List<BlobExpiry>?,
    val hints: List<String>? = listOf()
) {
    data class DomesticGreenCard(
        val origins: List<Origin>,
        val createCredentialMessages: ByteArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as DomesticGreenCard

            if (origins != other.origins) return false
            if (!createCredentialMessages.contentEquals(other.createCredentialMessages)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = origins.hashCode()
            result = 31 * result + createCredentialMessages.contentHashCode()
            return result
        }
    }

    data class EuGreenCard(
        val origins: List<Origin>,
        val credential: String
    )

    data class Origin(
        val type: OriginType,
        val eventTime: OffsetDateTime,
        val expirationTime: OffsetDateTime,
        val validFrom: OffsetDateTime,
        val doseNumber: Int?,
        val hints: List<String>? = listOf()
    )

    data class BlobExpiry(
        val id: Int,
        val expiry: OffsetDateTime
    )

    fun getEuOrigins(): List<OriginType> =
        euGreencards?.map { it.origins.map { it.type } }?.flatten() ?: listOf()

    fun getAllOrigins(): List<OriginType> {
        val origins = mutableListOf<OriginType>()
        origins.addAll(domesticGreencard?.origins?.map { it.type } ?: listOf())
        origins.addAll(euGreencards?.map { it.origins.map { it.type } }?.flatten() ?: listOf())
        return origins
    }
}
