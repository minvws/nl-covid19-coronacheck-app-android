/*
 *
 *  *  Copyright (c) 2022 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *  *
 *  *   SPDX-License-Identifier: EUPL-1.2
 *  *
 *
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.repositories

import nl.rijksoverheid.ctr.api.factory.NetworkRequestResultFactory
import nl.rijksoverheid.ctr.api.interceptors.SigningCertificate
import nl.rijksoverheid.ctr.holder.HolderStep
import nl.rijksoverheid.ctr.holder.ui.create_qr.api.MijnCnApiClient
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteProtocol3
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.SignedResponseWithModel
import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult

interface MijnCnEventProviderRepository {

    suspend fun getMijnCnEvents(
        url: String,
        token: String,
        signingCertificateBytes: ByteArray,
        filter: String,
        provider: String,
    ): NetworkRequestResult<SignedResponseWithModel<RemoteProtocol3>>
}

class MijnCnEventProviderRepositoryImpl(
    private val mijnCnApiClient: MijnCnApiClient,
    private val networkRequestResultFactory: NetworkRequestResultFactory,
) : MijnCnEventProviderRepository {

    override suspend fun getMijnCnEvents(
        url: String,
        token: String,
        signingCertificateBytes: ByteArray,
        filter: String,
        provider: String
    ): NetworkRequestResult<SignedResponseWithModel<RemoteProtocol3>> {
        return networkRequestResultFactory.createResult(
            step = HolderStep.EventNetworkRequest,
            provider = provider,
        ) {
            mijnCnApiClient.getEvents(
                url = url,
                authorization = "Bearer $token",
                params = mapOf("filter" to filter),
                certificate = SigningCertificate(signingCertificateBytes)
            )
        }
    }
}
