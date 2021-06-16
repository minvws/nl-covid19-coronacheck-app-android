/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig.api

import nl.rijksoverheid.ctr.api.signing.http.SignedRequest
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.api.model.PublicKeys
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming

interface AppConfigApi {
    @GET("config")
    @SignedRequest
    @Streaming
    suspend fun getConfig(): ResponseBody

    @GET("public_keys")
    @SignedRequest
    @Streaming
    suspend fun getPublicKeys(): ResponseBody
}
