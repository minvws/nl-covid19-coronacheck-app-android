/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.api

import net.openid.appauth.TokenResponse
import nl.rijksoverheid.ctr.api.interceptors.SigningCertificate
import nl.rijksoverheid.ctr.api.signing.http.SignedRequest
import nl.rijksoverheid.ctr.holder.ui.create_qr.mijncn.MijnCNTokenResponse
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.post.GetTestResultPostData
import org.json.JSONObject
import retrofit2.http.*

interface TestProviderApiClient {
    @POST
    @SignedRequest
    suspend fun getTestResult(
        @Url url: String,
        @Header("Authorization") authorization: String,
        @Header("CoronaCheck-Protocol-Version") protocolVersion: String = "3.0",
        @Body data: GetTestResultPostData?,
        @Tag certificate: SigningCertificate
    ): SignedResponseWithModel<RemoteProtocol>

    @POST
    @SignedRequest
    suspend fun getUnomi(
        @Url url: String,
        @Header("Authorization") authorization: String,
        @Header("CoronaCheck-Protocol-Version") protocolVersion: String = "3.0",
        @Body params: Map<String, String>,
        @Tag certificate: SigningCertificate,
    ): SignedResponseWithModel<RemoteUnomi>

    @POST
    @SignedRequest
    suspend fun getEvents(
        @Url url: String,
        @Header("Authorization") authorization: String,
        @Header("CoronaCheck-Protocol-Version") protocolVersion: String = "3.0",
        @Body params: Map<String, String>,
        @Tag certificate: SigningCertificate,
    ): SignedResponseWithModel<RemoteProtocol3>


    @POST
    @Headers("Content-Type: application/x-www-form-urlencoded", "Accept: application/json")
    @FormUrlEncoded
    suspend fun getAccessToken(
        @Url url: String,
        @Field("code") code : String,
        @Field("grant_type") grantType : String,
        @Field("redirect_uri") redirectUri : String,
        @Field("code_verifier") codeVerifier : String,
        @Field("client_id") clientId : String,
    ) : MijnCNTokenResponse

}
