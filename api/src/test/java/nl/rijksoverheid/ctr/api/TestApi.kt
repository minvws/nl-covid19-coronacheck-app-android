package nl.rijksoverheid.ctr.api

import nl.rijksoverheid.ctr.api.interceptors.CacheOverride
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

interface TestApi {
    @GET("/")
    @CacheOverride("public,max-age=0")
    suspend fun cacheOverridden(): Response<ResponseBody>

    @GET("/")
    suspend fun normalRequest(): Response<ResponseBody>
}