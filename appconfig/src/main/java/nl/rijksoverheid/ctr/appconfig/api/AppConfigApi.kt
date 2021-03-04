/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig.api

import nl.rijksoverheid.crt.signing.http.SignedRequest
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.api.model.PublicKeys
import retrofit2.http.GET

interface AppConfigApi {
    @GET("config")
    @SignedRequest
    suspend fun getConfig(): AppConfig

    @GET("public_keys")
    @SignedRequest
    suspend fun getPublicKeys(): PublicKeys
}
