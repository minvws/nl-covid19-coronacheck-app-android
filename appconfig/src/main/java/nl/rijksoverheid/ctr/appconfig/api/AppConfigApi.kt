/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig.api

import nl.rijksoverheid.ctr.api.signing.http.SignedRequest
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.GET

interface AppConfigApi {
    @GET("config")
    @SignedRequest
    suspend fun getConfig(): Response<ResponseBody>

    @GET("public_keys")
    @SignedRequest
    suspend fun getPublicKeys(): Response<JSONObject>
}
