/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.shared.api

import nl.rijksoverheid.crt.signing.http.SignedRequest
import nl.rijksoverheid.ctr.shared.models.RemoteTestResult
import nl.rijksoverheid.ctr.shared.models.SignedResponseWithModel
import nl.rijksoverheid.ctr.shared.models.post.GetTestResultPostData
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
        @Body data: GetTestResultPostData?,
        @Tag certificate: SigningCertificate
    ): SignedResponseWithModel<RemoteTestResult>
}