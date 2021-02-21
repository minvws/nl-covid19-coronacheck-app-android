package nl.rijksoverheid.ctr.holder.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.shared.api.SigningCertificate
import nl.rijksoverheid.ctr.shared.api.TestApiClient
import nl.rijksoverheid.ctr.shared.api.TestProviderApiClient
import nl.rijksoverheid.ctr.shared.models.RemoteNonce
import nl.rijksoverheid.ctr.shared.models.RemoteTestProviders
import nl.rijksoverheid.ctr.shared.models.RemoteTestResult
import nl.rijksoverheid.ctr.shared.models.ResponseError
import nl.rijksoverheid.ctr.shared.models.SignedResponseWithModel
import nl.rijksoverheid.ctr.shared.models.TestIsmResult
import nl.rijksoverheid.ctr.shared.models.post.GetTestIsmPostData
import nl.rijksoverheid.ctr.shared.models.post.GetTestResultPostData
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
    private val api: TestApiClient,
    private val testProviderApiClient: TestProviderApiClient,
    private val responseConverter: Converter<ResponseBody, SignedResponseWithModel<RemoteTestResult>>,
    private val errorResponseConverter: Converter<ResponseBody, ResponseError>
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
                    GetTestResultPostData(
                        it
                    )
                },
                certificate = SigningCertificate(signingCertificateBytes)
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

    suspend fun testProviders(): RemoteTestProviders {
        return api.getConfigCtp()
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun getTestIsm(test: String, sToken: String, icm: String): TestIsmResult {
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

    suspend fun remoteNonce(): RemoteNonce {
        return api.getNonce()
    }
}
