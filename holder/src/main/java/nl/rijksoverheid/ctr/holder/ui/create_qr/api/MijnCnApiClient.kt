/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.api

import nl.rijksoverheid.ctr.api.interceptors.SigningCertificate
import nl.rijksoverheid.ctr.api.signing.http.SignedRequest
import nl.rijksoverheid.ctr.holder.ui.create_qr.api.constant.ApiConstants.CORONACHECK_PROTOCOL_VERSION
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.MijnCNTokenResponse
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteProtocol3
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.SignedResponseWithModel
import retrofit2.http.*

interface MijnCnApiClient {

    @POST
    @Headers("Content-Type: application/x-www-form-urlencoded", "Accept: application/json")
    @FormUrlEncoded
    suspend fun getAccessToken(
        @Url url: String,
        @Field("code") code: String,
        @Field("grant_type") grantType: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("code_verifier") codeVerifier: String,
        @Field("client_id") clientId: String,
    ): MijnCNTokenResponse

    @POST
    @SignedRequest
    @Headers("User-Agent: ") // Empty user agent necessary for BES Islands events
    suspend fun getEvents(
        @Url url: String,
        @Header("Authorization") authorization: String,
        @Header("CoronaCheck-Protocol-Version") protocolVersion: String = CORONACHECK_PROTOCOL_VERSION,
        @Body params: Map<String, String>,
        @Tag certificate: SigningCertificate,
    ): SignedResponseWithModel<RemoteProtocol3>
}
