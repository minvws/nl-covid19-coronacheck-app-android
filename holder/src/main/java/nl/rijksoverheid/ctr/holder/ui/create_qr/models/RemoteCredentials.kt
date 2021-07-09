package nl.rijksoverheid.ctr.holder.ui.create_qr.models

import com.squareup.moshi.JsonClass
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
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
    val euGreencards: List<EuGreenCard>?
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
        val validFrom: OffsetDateTime
    )

    fun getAllOrigins(): List<OriginType> {
        val origins = mutableListOf<OriginType>()
        origins.addAll(domesticGreencard?.origins?.map { it.type } ?: listOf())
        origins.addAll(euGreencards?.map { it.origins.map { it.type } }?.flatten() ?: listOf())
        return origins
    }
}
