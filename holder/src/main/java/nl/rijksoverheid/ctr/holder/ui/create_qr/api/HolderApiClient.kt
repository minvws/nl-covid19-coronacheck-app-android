package nl.rijksoverheid.ctr.holder.ui.create_qr.api

import nl.rijksoverheid.ctr.api.signing.http.SignedRequest
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteAccessTokens
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteConfigProviders
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteCredentials
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteNonce
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.post.AccessTokenPostData
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.post.GetTestIsmPostData
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface HolderApiClient {
    @GET("holder/nonce")
    @SignedRequest
    suspend fun getNonce(): RemoteNonce

    @POST("get_credentials")
    suspend fun getCredentials(): RemoteCredentials

    @GET("holder/config_providers")
    @SignedRequest
    suspend fun getConfigCtp(): RemoteConfigProviders

    @POST("holder/access_tokens")
    @SignedRequest
    suspend fun getAccessTokens(@Body data: AccessTokenPostData): RemoteAccessTokens

    @POST("holder/get_test_ism")
    @SignedRequest
    suspend fun getTestIsm(
        @Body data: GetTestIsmPostData
    ): Response<ResponseBody>
}
