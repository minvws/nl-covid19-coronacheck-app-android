package nl.rijksoverheid.ctr.holder.api

import nl.rijksoverheid.ctr.api.signing.http.SignedRequest
import nl.rijksoverheid.ctr.holder.api.post.GetCouplingData
import nl.rijksoverheid.ctr.holder.api.post.GetCredentialsPostData
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteAccessTokens
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteConfigProviders
import nl.rijksoverheid.ctr.holder.paper_proof.models.RemoteCouplingResponse
import nl.rijksoverheid.ctr.holder.your_events.models.RemoteGreenCards
import nl.rijksoverheid.ctr.holder.your_events.models.RemotePrepareIssue
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface HolderApiClient {
    @GET("holder/prepare_issue")
    suspend fun getPrepareIssue(): RemotePrepareIssue

    @POST("holder/get_credentials")
    suspend fun getCredentials(
        @Body data: GetCredentialsPostData
    ): RemoteGreenCards

    @POST("holder/access_tokens")
    suspend fun getAccessTokens(@Header("Authorization") authorization: String): RemoteAccessTokens

    @POST("holder/coupling")
    suspend fun getCoupling(@Body data: GetCouplingData): RemoteCouplingResponse
}
