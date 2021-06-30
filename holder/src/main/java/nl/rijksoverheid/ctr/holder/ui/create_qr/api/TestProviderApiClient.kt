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
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.post.GetTestResultPostData
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
    suspend fun unomiVaccinationEvents(
        @Url url: String,
        @Header("Authorization") authorization: String,
        @Header("CoronaCheck-Protocol-Version") protocolVersion: String = "3.0",
        @Body params: Map<String, String> = mapOf("filter" to "vaccination"),
        @Tag certificate: SigningCertificate,
    ): SignedResponseWithModel<RemoteUnomi>

    @POST
    @SignedRequest
    suspend fun unomiTestEvents(
        @Url url: String,
        @Header("Authorization") authorization: String,
        @Header("CoronaCheck-Protocol-Version") protocolVersion: String = "3.0",
        @Body params: Map<String, String> = mapOf("filter" to "negativetest"),
        @Tag certificate: SigningCertificate,
    ): SignedResponseWithModel<RemoteUnomi>

    @POST
    @SignedRequest
    suspend fun unomiPositiveAndRecoveryTestEvents(
        @Url url: String,
        @Header("Authorization") authorization: String,
        @Header("CoronaCheck-Protocol-Version") protocolVersion: String = "3.0",
        @Body params: Map<String, String> = mapOf("filter" to "positivetest,recovery"),
        @Tag certificate: SigningCertificate,
    ): SignedResponseWithModel<RemoteUnomi>

    @POST
    @SignedRequest
    suspend fun vaccinationEvents(
        @Url url: String,
        @Header("Authorization") authorization: String,
        @Header("CoronaCheck-Protocol-Version") protocolVersion: String = "3.0",
        @Body params: Map<String, String> = mapOf("filter" to "vaccination"),
        @Tag certificate: SigningCertificate,
        ): SignedResponseWithModel<RemoteProtocol3>

    @POST
    @SignedRequest
    suspend fun negativeTestEvents(
        @Url url: String,
        @Header("Authorization") authorization: String,
        @Header("CoronaCheck-Protocol-Version") protocolVersion: String = "3.0",
        @Body params: Map<String, String> = mapOf("filter" to "negativetest"),
        @Tag certificate: SigningCertificate,
        ): SignedResponseWithModel<RemoteProtocol3>

    @POST
    @SignedRequest
    suspend fun positiveAndRecoveryEvents(
        @Url url: String,
        @Header("Authorization") authorization: String,
        @Header("CoronaCheck-Protocol-Version") protocolVersion: String = "3.0",
        @Body params: Map<String, String> = mapOf("filter" to "positivetest,recovery"),
        @Tag certificate: SigningCertificate,
    ): SignedResponseWithModel<RemoteProtocol3>
}
