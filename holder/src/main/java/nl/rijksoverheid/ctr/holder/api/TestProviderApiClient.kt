/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.api

import nl.rijksoverheid.ctr.api.signing.http.SignedRequest
import nl.rijksoverheid.ctr.api.interceptors.SigningCertificate
import nl.rijksoverheid.ctr.holder.models.RemoteTestResult
import nl.rijksoverheid.ctr.holder.models.SignedResponseWithModel
import nl.rijksoverheid.ctr.holder.models.post.GetTestResultPostData
import retrofit2.http.*

interface TestProviderApiClient {
    @POST
    @SignedRequest
    suspend fun getTestResult(
        @Url url: String,
        @Header("Authorization") authorization: String,
        @Header("CoronaCheck-Protocol-Version") protocolVersion: String,
        @Body data: GetTestResultPostData?,
        @Tag certificate: SigningCertificate
    ): SignedResponseWithModel<RemoteTestResult>
}
