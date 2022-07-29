/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.get_events.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@JsonClass(generateAdapter = true)
data class RemoteConfigProviders(
    @Json(name = "tokenProviders") val testProviders: List<TestProvider>,
    @Json(name = "eventProviders") val eventProviders: List<EventProvider>,
    @Json(name = "eventProvidersBes") val eventProvidersBes: List<EventProvider>
) {

    @JsonClass(generateAdapter = true)
    data class TestProvider(
        val name: String,
        @Json(name = "identifier") val providerIdentifier: String,
        @Json(name = "url") val resultUrl: String,
        @Json(name = "cms") val cms: List<ByteArray>,
        @Json(name = "tls") val tls: List<ByteArray>,
        @Json(name = "usage") val usage: List<String>
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as TestProvider

            if (name != other.name) return false
            if (providerIdentifier != other.providerIdentifier) return false
            if (resultUrl != other.resultUrl) return false
            if (!cms.first().contentEquals(other.cms.first())) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + providerIdentifier.hashCode()
            result = 31 * result + resultUrl.hashCode()
            result = 31 * result + cms.first().contentHashCode()
            return result
        }
    }

    data class EventProvider(
        val name: String,
        @Json(name = "identifier") val providerIdentifier: String,
        @Json(name = "unomiUrl") val unomiUrl: String,
        @Json(name = "eventUrl") val eventUrl: String,
        val cms: List<ByteArray>,
        val tls: List<ByteArray>,
        val usage: List<String>,
        val auth: List<String>
    ) {
        fun supports(originType: RemoteOriginType, loginType: LoginType): Boolean {
            return when (originType) {
                RemoteOriginType.Recovery -> usage.contains("r") && auth.contains(getAuthForLoginType(loginType))
                RemoteOriginType.Test -> (usage.contains("nt") || usage.contains("pt")) && auth.contains(getAuthForLoginType(loginType))
                RemoteOriginType.Vaccination -> usage.contains("v") && auth.contains(getAuthForLoginType(loginType))
            }
        }

        private fun getAuthForLoginType(loginType: LoginType): String {
            return when (loginType) {
                is LoginType.Pap -> "pap"
                is LoginType.Max -> "max"
            }
        }

        companion object {
            const val PROVIDER_IDENTIFIER_DCC = "dcc"
            const val PROVIDER_IDENTIFIER_DCC_SUFFIX = "dcc_[unique]"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as EventProvider

            if (name != other.name) return false
            if (providerIdentifier != other.providerIdentifier) return false
            if (unomiUrl != other.unomiUrl) return false
            if (eventUrl != other.eventUrl) return false
            if (!cms.first().contentEquals(other.cms.first())) return false
            if (!tls.first().contentEquals(other.tls.first())) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + providerIdentifier.hashCode()
            result = 31 * result + unomiUrl.hashCode()
            result = 31 * result + eventUrl.hashCode()
            result = 31 * result + cms.first().contentHashCode()
            result = 31 * result + tls.first().contentHashCode()
            return result
        }
    }
}
