/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.ctr.api.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Invocation

class CacheOverrideInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val invocation = chain.request().tag(Invocation::class.java)
        val annotation = invocation?.method()?.getAnnotation(CacheOverride::class.java)
        val response = chain.proceed(chain.request())
        return if (annotation != null) {
            if (response.isSuccessful && response.cacheResponse == null) {
                response.newBuilder()
                    .removeHeader("cache-control")
                    .removeHeader("pragma")
                    .addHeader("cache-control", annotation.cacheHeaderValue).build()
            } else {
                response
            }
        } else {
            response
        }
    }
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class CacheOverride(val cacheHeaderValue: String)
