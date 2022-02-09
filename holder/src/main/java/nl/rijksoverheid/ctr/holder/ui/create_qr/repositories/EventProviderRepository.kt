package nl.rijksoverheid.ctr.holder.ui.create_qr.repositories

import nl.rijksoverheid.ctr.api.factory.NetworkRequestResultFactory
import nl.rijksoverheid.ctr.api.interceptors.SigningCertificate
import nl.rijksoverheid.ctr.holder.HolderStep
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.api.TestProviderApiClient
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteOriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteProtocol3
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteUnomi
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.SignedResponseWithModel
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
        fun getFilter(originType: RemoteOriginType): String {
            return when (originType) {
                is RemoteOriginType.Vaccination -> {
                    "vaccination"
                }
                // TODO Change to positivetest only when GGD accepts scope parameter
                is RemoteOriginType.Recovery -> {
                    "positivetest,recovery"
                }
                is RemoteOriginType.Test -> {
                    "negativetest"
                }
            }
        }
    }

    suspend fun getUnomi(
        url: String,
        token: String,
        filter: String,
        scope: String?,
        signingCertificateBytes: ByteArray,
        provider: String,
    ): NetworkRequestResult<RemoteUnomi>

    suspend fun getEvents(
        url: String,
        token: String,
        signingCertificateBytes: ByteArray,
        filter: String,
        scope: String?,
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
        scope: String?,
        signingCertificateBytes: ByteArray,
        provider: String,
    ): NetworkRequestResult<RemoteUnomi> {
        val params = mutableMapOf<String, String>()
        params["filter"] = filter
        // TODO Enable when GGD accepts scope parameter
//        scope?.let {
//            params["scope"] = scope
//        }

        return networkRequestResultFactory.createResult(
            step = HolderStep.UnomiNetworkRequest,
            provider = provider,
        ) {
            testProviderApiClient.getUnomi(
                url = url,
                authorization = "Bearer $token",
                params = params,
                certificate = SigningCertificate(signingCertificateBytes)
            ).model
        }
    }

    override suspend fun getEvents(
        url: String,
        token: String,
        signingCertificateBytes: ByteArray,
        filter: String,
        scope: String?,
        provider: String,
    ): NetworkRequestResult<SignedResponseWithModel<RemoteProtocol3>> {
        val params = mutableMapOf<String, String>()
        params["filter"] = filter
        // TODO Enable when GGD accepts scope parameter
//        scope?.let {
//            params["scope"] = scope
//        }

        return networkRequestResultFactory.createResult(
            step = HolderStep.EventNetworkRequest,
            provider = provider,
        ) {
            testProviderApiClient.getEvents(
                url = url,
                authorization = "Bearer $token",
                params = params,
                certificate = SigningCertificate(signingCertificateBytes)
            )
        }
    }
}
