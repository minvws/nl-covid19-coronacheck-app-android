package nl.rijksoverheid.ctr.api

import nl.rijksoverheid.crt.signing.http.SignedRequest
import nl.rijksoverheid.ctr.api.models.RemoteNonce
import nl.rijksoverheid.ctr.api.models.RemoteTestProviders
import nl.rijksoverheid.ctr.api.models.post.GetTestIsmPostData
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
interface CoronaCheckApiClient {
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
