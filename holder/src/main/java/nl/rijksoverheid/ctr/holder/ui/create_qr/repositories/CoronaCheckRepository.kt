package nl.rijksoverheid.ctr.holder.ui.create_qr.repositories

import android.util.Base64
import nl.rijksoverheid.ctr.api.factory.NetworkRequestResultFactory
import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult
import nl.rijksoverheid.ctr.holder.HolderStep
import nl.rijksoverheid.ctr.holder.ui.create_qr.api.HolderApiClient
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.post.GetCouplingData
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.post.GetCredentialsPostData

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

interface CoronaCheckRepository {
    suspend fun configProviders(): NetworkRequestResult<RemoteConfigProviders>
    suspend fun accessTokens(jwt: String): NetworkRequestResult<RemoteAccessTokens>
    suspend fun getGreenCards(
        stoken: String,
        events: List<String>,
        issueCommitmentMessage: String
    ): NetworkRequestResult<RemoteGreenCards>

    suspend fun getPrepareIssue(): NetworkRequestResult<RemotePrepareIssue>
    suspend fun getCoupling(credential: String, couplingCode: String): NetworkRequestResult<RemoteCouplingResponse>
}

open class CoronaCheckRepositoryImpl(
    private val api: HolderApiClient,
    private val networkRequestResultFactory: NetworkRequestResultFactory
) : CoronaCheckRepository {

    override suspend fun configProviders(): NetworkRequestResult<RemoteConfigProviders> {
        return networkRequestResultFactory.createResult(HolderStep.ConfigProvidersNetworkRequest) {
            api.getConfigCtp()
        }
    }

    override suspend fun accessTokens(jwt: String): NetworkRequestResult<RemoteAccessTokens> {
        return networkRequestResultFactory.createResult(HolderStep.AccessTokensNetworkRequest) {
            api.getAccessTokens(authorization = "Bearer $jwt")
        }
    }

    override suspend fun getGreenCards(
        stoken: String,
        events: List<String>,
        issueCommitmentMessage: String
    ): NetworkRequestResult<RemoteGreenCards> {
        return networkRequestResultFactory.createResult(HolderStep.GetCredentialsNetworkRequest) {
            api.getCredentials(
                data = GetCredentialsPostData(
                    stoken = stoken,
                    events = events,
                    issueCommitmentMessage = Base64.encodeToString(
                        issueCommitmentMessage.toByteArray(),
                        Base64.NO_WRAP
                    )
                )
            )
        }
    }

    override suspend fun getPrepareIssue(): NetworkRequestResult<RemotePrepareIssue> {
        return networkRequestResultFactory.createResult(HolderStep.PrepareIssueNetworkRequest) {
            api.getPrepareIssue()
        }
    }

    override suspend fun getCoupling(credential: String,
                                     couplingCode: String): NetworkRequestResult<RemoteCouplingResponse> {
        return networkRequestResultFactory.createResult(HolderStep.CouplingNetworkRequest) {
            api.getCoupling(
                data = GetCouplingData(
                    credential = credential,
                    couplingCode = couplingCode
                )
            )
        }
    }
}
