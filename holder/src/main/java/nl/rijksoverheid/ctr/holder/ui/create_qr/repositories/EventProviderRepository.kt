package nl.rijksoverheid.ctr.holder.ui.create_qr.repositories

import nl.rijksoverheid.ctr.api.factory.NetworkRequestResultFactory
import nl.rijksoverheid.ctr.api.interceptors.SigningCertificate
import nl.rijksoverheid.ctr.holder.HolderStep
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.api.TestProviderApiClient
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.*
import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface EventProviderRepository {
    companion object {
        /**
         * Get filter for backend endpoints
         */
        fun getFilter(originType: OriginType): String {
            return when (originType) {
                is OriginType.Vaccination -> {
                    "vaccination"
                }
                is OriginType.Recovery -> {
                    "positivetest,recovery"
                }
                is OriginType.Test -> {
                    "negativetest"
                }
            }
        }
    }

    suspend fun getUnomi(
        url: String,
        token: String,
        filter: String,
        signingCertificateBytes: ByteArray,
        provider: String,
    ): NetworkRequestResult<RemoteUnomi>

    suspend fun getEvents(
        url: String,
        token: String,
        signingCertificateBytes: ByteArray,
        filter: String,
        provider: String,
    ): NetworkRequestResult<SignedResponseWithModel<RemoteProtocol3>>
}

class EventProviderRepositoryImpl(
    private val testProviderApiClient: TestProviderApiClient,
    private val networkRequestResultFactory: NetworkRequestResultFactory,
) : EventProviderRepository {

    override suspend fun getUnomi(
        url: String,
        token: String,
        filter: String,
        signingCertificateBytes: ByteArray,
        provider: String,
    ): NetworkRequestResult<RemoteUnomi> {
        return networkRequestResultFactory.createResult(
            step = HolderStep.UnomiNetworkRequest,
            provider = provider,
        ) {
            testProviderApiClient.getUnomi(
                url = url,
                authorization = "Bearer $token",
                params = mapOf("filter" to filter),
                certificate = SigningCertificate(signingCertificateBytes)
            ).model
        }
    }

    override suspend fun getEvents(
        url: String,
        token: String,
        signingCertificateBytes: ByteArray,
        filter: String,
        provider: String,
    ): NetworkRequestResult<SignedResponseWithModel<RemoteProtocol3>> {
        return networkRequestResultFactory.createResult(
            step = HolderStep.EventNetworkRequest,
            provider = provider,
        ) {
            testProviderApiClient.getEvents(
                url = url,
                authorization = "Bearer $token",
                params = mapOf("filter" to filter),
                certificate = SigningCertificate(signingCertificateBytes)
            )
        }
    }
}
