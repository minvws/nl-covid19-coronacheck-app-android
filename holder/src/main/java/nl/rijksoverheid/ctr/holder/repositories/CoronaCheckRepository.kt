package nl.rijksoverheid.ctr.holder.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.api.CoronaCheckApiClient
import nl.rijksoverheid.ctr.api.models.RemoteNonce
import nl.rijksoverheid.ctr.api.models.RemoteTestProviders
import nl.rijksoverheid.ctr.api.models.ResponseError
import nl.rijksoverheid.ctr.api.models.TestIsmResult
import nl.rijksoverheid.ctr.api.models.post.GetTestIsmPostData
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Converter
import retrofit2.HttpException

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

interface CoronaCheckRepository {
    suspend fun testProviders(): RemoteTestProviders
    suspend fun getTestIsm(test: String, sToken: String, icm: String): TestIsmResult
    suspend fun remoteNonce(): RemoteNonce
}

open class CoronaCheckRepositoryImpl(
    private val api: CoronaCheckApiClient,
    private val errorResponseConverter: Converter<ResponseBody, ResponseError>
) : CoronaCheckRepository {

    override suspend fun testProviders(): RemoteTestProviders {
        return api.getConfigCtp()
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun getTestIsm(
        test: String,
        sToken: String,
        icm: String
    ): TestIsmResult {
        val response = api.getTestIsm(
            GetTestIsmPostData(
                test = test,
                sToken = sToken,
                icm = JSONObject(icm).toString()
            )
        )

        return if (response.isSuccessful) {
            val body =
                response.body()?.string()
                    ?: throw IllegalStateException("Body should not be null")
            TestIsmResult.Success(body)
        } else {
            val errorBody = response.errorBody() ?: throw HttpException(response)
            withContext(Dispatchers.IO) {
                val responseError =
                    errorResponseConverter.convert(errorBody) ?: throw HttpException(response)
                TestIsmResult.Error(responseError)
            }
        }
    }

    override suspend fun remoteNonce(): RemoteNonce {
        return api.getNonce()
    }
}
