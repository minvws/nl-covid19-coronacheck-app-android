package nl.rijksoverheid.ctr.holder.ui.create_qr.api

import nl.rijksoverheid.ctr.api.signing.http.SignedRequest
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.post.GetCouplingData
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.post.GetCredentialsPostData
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.post.GetTestIsmPostData
import okhttp3.ResponseBody
import retrofit2.Response
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

    @GET("holder/config_providers")
    @SignedRequest
    suspend fun getConfigCtp(): RemoteConfigProviders

    @POST("holder/access_tokens")
    suspend fun getAccessTokens(@Header("Authorization") authorization: String): RemoteAccessTokens

    @POST("holder/coupling")
    suspend fun getCoupling(@Body data: GetCouplingData): RemoteCouplingResponse
}
