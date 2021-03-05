package nl.rijksoverheid.ctr.appconfig.api

import nl.rijksoverheid.ctr.appconfig.CachedAppConfigUseCase
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.TimeUnit

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

/**
 * Always try to get config files fresh from remote
 * If request fails, try to get from cache based if before config ttl seconds expire date
 */
class AppConfigApiCacheInterceptor(private val cachedAppConfigUseCase: CachedAppConfigUseCase) :
    Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = try {
            chain.proceed(chain.request())
        } catch (ex: IOException) {
            Timber.d("Network request failed")
            null
        }
        return if (response?.isSuccessful == true) {
            response
        } else {
            val configTimeToLive = try {
                cachedAppConfigUseCase.getCachedAppConfig().configTtlSeconds
            } catch (e: Exception) {
                0
            }

            val cacheControl = CacheControl
                .Builder()
                .onlyIfCached()
                .maxStale(configTimeToLive, TimeUnit.SECONDS)
                .build()

            chain.proceed(
                chain.request().newBuilder().cacheControl(cacheControl).build()
            )
        }
    }
}
