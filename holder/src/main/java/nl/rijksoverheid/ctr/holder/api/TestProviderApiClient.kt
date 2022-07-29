/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.api

import nl.rijksoverheid.ctr.api.interceptors.SigningCertificate
import nl.rijksoverheid.ctr.api.signing.http.SignedRequest
import nl.rijksoverheid.ctr.holder.api.models.SignedResponseWithModel
import nl.rijksoverheid.ctr.holder.api.post.GetTestResultPostData
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteUnomi
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Tag
import retrofit2.http.Url

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
        @Tag certificate: SigningCertificate
    ): SignedResponseWithModel<RemoteUnomi>

    @POST
    @SignedRequest
    suspend fun getEvents(
        @Url url: String,
        @Header("Authorization") authorization: String,
        @Header("CoronaCheck-Protocol-Version") protocolVersion: String = "3.0",
        @Body params: Map<String, String>,
        @Tag certificate: SigningCertificate
    ): SignedResponseWithModel<RemoteProtocol>
}
