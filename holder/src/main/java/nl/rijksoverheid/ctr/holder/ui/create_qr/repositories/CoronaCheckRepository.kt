package nl.rijksoverheid.ctr.holder.ui.create_qr.repositories

import android.util.Base64
import nl.rijksoverheid.ctr.holder.ui.create_qr.api.HolderApiClient
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.post.GetCouplingData
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.post.GetCredentialsPostData
import okhttp3.ResponseBody
import retrofit2.Converter

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

interface CoronaCheckRepository {
    suspend fun configProviders(): RemoteConfigProviders
    suspend fun accessTokens(jwt: String): RemoteAccessTokens
    suspend fun getGreenCards(
        stoken: String,
        events: List<String>,
        issueCommitmentMessage: String
    ): RemoteGreenCards

    suspend fun getPrepareIssue(): RemotePrepareIssue
    suspend fun getCoupling(credential: String, couplingCode: String): RemoteCouplingResponse
}

open class CoronaCheckRepositoryImpl(
    private val api: HolderApiClient,
    private val errorResponseConverter: Converter<ResponseBody, ResponseError>
) : CoronaCheckRepository {

    override suspend fun configProviders(): RemoteConfigProviders {
        return api.getConfigCtp()
    }

    override suspend fun accessTokens(jwt: String): RemoteAccessTokens {
        return api.getAccessTokens(authorization = "Bearer $jwt")
    }

    override suspend fun getGreenCards(
        stoken: String,
        events: List<String>,
        issueCommitmentMessage: String
    ): RemoteGreenCards {
        return api.getCredentials(
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

    override suspend fun getPrepareIssue(): RemotePrepareIssue {
        return api.getPrepareIssue()
    }

    override suspend fun getCoupling(credential: String, couplingCode: String): RemoteCouplingResponse {
        return api.getCoupling(
            data = GetCouplingData(
                credential = credential,
                couplingCode = couplingCode
            )
        )
    }
}
