/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.network

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import nl.rijksoverheid.ctr.BuildConfig
import nl.rijksoverheid.ctr.config.AppConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.*

interface StubbedAPI {

    @GET("config")
    @Streaming
    suspend fun getAppConfig(): Response<AppConfig>
    
    companion object {
        fun create(
            context: Context,
            client: OkHttpClient = createOkHttpClient(context),
            baseUrl: String = BuildConfig.BASE_API_URL
        ): StubbedAPI {
            val contentType = "application/json".toMediaType()
            return Retrofit.Builder()
                .client(client)
                .addConverterFactory(Defaults.json.asConverterFactory(contentType))
                .baseUrl(baseUrl)
                .build().create(StubbedAPI::class.java)
        }
    }

    object Defaults {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

}