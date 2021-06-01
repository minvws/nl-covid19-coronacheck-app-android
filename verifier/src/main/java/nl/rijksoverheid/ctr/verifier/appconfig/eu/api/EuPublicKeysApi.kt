/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig.eu.api

import nl.rijksoverheid.ctr.api.signing.http.SignedRequest
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming

interface EuPublicKeysApi {
    @GET("get_public_keys")
    @SignedRequest
    @Streaming
    suspend fun getPublicKeys(): ResponseBody

    @GET("config")
    @SignedRequest
    @Streaming
    suspend fun config(): ResponseBody
}