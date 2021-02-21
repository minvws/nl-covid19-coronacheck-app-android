package nl.rijksoverheid.ctr.holder.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.api.CoronaCheckApiClient
import nl.rijksoverheid.ctr.api.TestProviderApiClient
import nl.rijksoverheid.ctr.api.models.RemoteTestResult
import nl.rijksoverheid.ctr.api.models.SignedResponseWithModel
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
class HolderRepository(
    private val api: CoronaCheckApiClient,
    private val testProviderApiClient: TestProviderApiClient,
    private val responseConverter: Converter<ResponseBody, SignedResponseWithModel<RemoteTestResult>>,
    private val errorResponseConverter: Converter<ResponseBody, nl.rijksoverheid.ctr.api.models.ResponseError>
) {

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun remoteTestResult(
        url: String,
        token: String,
        verifierCode: String?,
        signingCertificateBytes: ByteArray
    ): SignedResponseWithModel<RemoteTestResult> {
        try {
            return testProviderApiClient.getTestResult(
                url = url,
                authorization = "Bearer $token",
                data = verifierCode?.let {
                    nl.rijksoverheid.ctr.api.models.post.GetTestResultPostData(
                        it
                    )
                },
                certificate = nl.rijksoverheid.ctr.api.SigningCertificate(signingCertificateBytes)
            )
        } catch (ex: HttpException) {
            // if there's no error body, this must be something else than expected
            val errorBody = ex.response()?.errorBody() ?: throw ex
            if (ex.code() == 401) {
                return withContext(Dispatchers.IO) {
                    responseConverter.convert(errorBody) ?: throw ex
                }
            } else {
                throw ex
            }
        }
    }

    suspend fun testProviders(): nl.rijksoverheid.ctr.api.models.RemoteTestProviders {
        return api.getConfigCtp()
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun getTestIsm(
        test: String,
        sToken: String,
        icm: String
    ): nl.rijksoverheid.ctr.api.models.TestIsmResult {
        val response = api.getTestIsm(
            nl.rijksoverheid.ctr.api.models.post.GetTestIsmPostData(
                test = test,
                sToken = sToken,
                icm = JSONObject(icm).toString()
            )
        )

        return if (response.isSuccessful) {
            val body =
                response.body()?.string()
                    ?: throw IllegalStateException("Body should not be null")
            nl.rijksoverheid.ctr.api.models.TestIsmResult.Success(body)
        } else {
            val errorBody = response.errorBody() ?: throw HttpException(response)
            withContext(Dispatchers.IO) {
                val responseError =
                    errorResponseConverter.convert(errorBody) ?: throw HttpException(response)
                nl.rijksoverheid.ctr.api.models.TestIsmResult.Error(responseError)
            }
        }
    }

    suspend fun remoteNonce(): nl.rijksoverheid.ctr.api.models.RemoteNonce {
        return api.getNonce()
    }
}
