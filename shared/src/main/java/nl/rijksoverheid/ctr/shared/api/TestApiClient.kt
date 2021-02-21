package nl.rijksoverheid.ctr.shared.api

import nl.rijksoverheid.crt.signing.http.SignedRequest
import nl.rijksoverheid.ctr.shared.models.*
import nl.rijksoverheid.ctr.shared.models.post.GetTestIsmPostData
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface TestApiClient {

    @GET("holder/get_public_keys")
    @SignedRequest
    suspend fun getIssuers(): Issuers

    @GET("issuer/get_event/{id}")
    @SignedRequest
    suspend fun getEvent(@Path("id") id: String): RemoteEvent

    @GET("issuer/get_agent/{id}")
    @SignedRequest
    suspend fun getAgent(@Path("id") id: String): RemoteAgent

    @GET("holder/nonce")
    @SignedRequest
    suspend fun getNonce(): RemoteNonce

    @GET("holder/config_ctp")
    @SignedRequest
    suspend fun getConfigCtp(): RemoteTestProviders

    @POST("holder/get_test_ism")
    @SignedRequest
    suspend fun getTestIsm(
        @Body data: GetTestIsmPostData
    ): Response<ResponseBody>
}
