/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig.api

import nl.rijksoverheid.crt.signing.http.SignedRequest
import nl.rijksoverheid.ctr.api.cache.CacheOverride
import nl.rijksoverheid.ctr.api.cache.CacheStrategy
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.api.model.PublicKeys
import retrofit2.http.GET
import retrofit2.http.Tag

interface AppConfigApi {
    @GET("config")
    @CacheOverride("public,max-age=0")
    @SignedRequest
    suspend fun getConfig(@Tag cacheStrategy: CacheStrategy = CacheStrategy.CACHE_FIRST): AppConfig

    @GET("public_keys")
    @CacheOverride("public,max-age=0")
    @SignedRequest
    suspend fun getPublicKeys(@Tag cacheStrategy: CacheStrategy = CacheStrategy.CACHE_FIRST): PublicKeys
}
