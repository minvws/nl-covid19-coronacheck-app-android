package nl.rijksoverheid.ctr.holder.ui.create_qr.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@JsonClass(generateAdapter = true)
data class RemoteConfigProviders(
    @Json(name = "corona_test_providers") val testProviders: List<TestProvider>,
    @Json(name = "event_providers") val eventProviders: List<EventProvider>
) {

    @JsonClass(generateAdapter = true)
    data class TestProvider(
        val name: String,
        @Json(name = "provider_identifier") val providerIdentifier: String,
        @Json(name = "result_url") val resultUrl: String,
        @Json(name = "public_key") val publicKey: ByteArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as TestProvider

            if (name != other.name) return false
            if (providerIdentifier != other.providerIdentifier) return false
            if (resultUrl != other.resultUrl) return false
            if (!publicKey.contentEquals(other.publicKey)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + providerIdentifier.hashCode()
            result = 31 * result + resultUrl.hashCode()
            result = 31 * result + publicKey.contentHashCode()
            return result
        }
    }

    data class EventProvider(
        val name: String,
        @Json(name = "provider_identifier") val providerIdentifier: String,
        @Json(name = "unomi_url") val unomiUrl: String,
        @Json(name = "event_url") val eventUrl: String,
        val cms: ByteArray,
        val tls: ByteArray,
        val usage: List<String>,
    ) {
        fun supports(originType: OriginType): Boolean {
            return when (originType) {
                OriginType.Recovery -> usage.contains("r")
                OriginType.Test -> usage.contains("nt") || usage.contains("pt")
                OriginType.Vaccination -> usage.contains("v")
            }
        }
        companion object {
            const val PROVIDER_IDENTIFIER_DCC = "dcc"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as EventProvider

            if (name != other.name) return false
            if (providerIdentifier != other.providerIdentifier) return false
            if (unomiUrl != other.unomiUrl) return false
            if (eventUrl != other.eventUrl) return false
            if (!cms.contentEquals(other.cms)) return false
            if (!tls.contentEquals(other.tls)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + providerIdentifier.hashCode()
            result = 31 * result + unomiUrl.hashCode()
            result = 31 * result + eventUrl.hashCode()
            result = 31 * result + cms.contentHashCode()
            result = 31 * result + tls.contentHashCode()
            return result
        }
    }
}
