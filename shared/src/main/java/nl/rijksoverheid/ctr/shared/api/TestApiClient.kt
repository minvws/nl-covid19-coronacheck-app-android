package nl.rijksoverheid.ctr.shared.api

import nl.rijksoverheid.ctr.shared.models.Config
import nl.rijksoverheid.ctr.shared.models.Issuers
import nl.rijksoverheid.ctr.shared.models.RemoteAgent
import nl.rijksoverheid.ctr.shared.models.RemoteEvent
import nl.rijksoverheid.ctr.shared.models.RemoteNonce
import nl.rijksoverheid.ctr.shared.models.RemoteTestProviders
import nl.rijksoverheid.ctr.shared.models.RemoteTestResult
import nl.rijksoverheid.ctr.shared.models.post.GetTestIsmPostData
import nl.rijksoverheid.ctr.shared.models.post.GetTestResultPostData
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Tag
import retrofit2.http.Url

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface TestApiClient {

    @GET("/holder/get_public_keys/")
    suspend fun getIssuers(): Issuers

    @GET("/issuer/get_event/{id}")
    suspend fun getEvent(@Path("id") id: String): RemoteEvent

    @GET("issuer/get_agent/{id}")
    suspend fun getAgent(@Path("id") id: String): RemoteAgent

    @GET("/holder/get_nonce/")
    suspend fun getNonce(): RemoteNonce

    @GET("/holder/config_ctp/")
    suspend fun getConfigCtp(): RemoteTestProviders

    @GET("holder/config/")
    suspend fun getHolderConfig(): Config

    @GET("verifier/config/")
    suspend fun getVerifierConfig(): Config

    @POST("/holder/get_test_ism/")
    suspend fun getTestIsm(
        @Body data: GetTestIsmPostData
    ): Response<ResponseBody>

    @POST
    suspend fun getTestResult(
        @Url url: String,
        @Header("Authorization") authorization: String,
        @Body data: GetTestResultPostData?,
        @Tag certificate: SigningCertificate
    ): RemoteTestResult
}
