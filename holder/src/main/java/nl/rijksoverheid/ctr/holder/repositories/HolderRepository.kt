package nl.rijksoverheid.ctr.holder.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.shared.api.SigningCertificate
import nl.rijksoverheid.ctr.shared.api.TestApiClient
import nl.rijksoverheid.ctr.shared.models.RemoteNonce
import nl.rijksoverheid.ctr.shared.models.RemoteTestProviders
import nl.rijksoverheid.ctr.shared.models.RemoteTestResult
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
    private val responseConverter: Converter<ResponseBody, RemoteTestResult>
) {

    suspend fun remoteTestResult(
        url: String,
        token: String,
        verifierCode: String?,
        signingCertificateBytes: ByteArray
    ): RemoteTestResult {
        try {
            return api.getTestResult(
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
            if (ex.code() == 401 || ex.code() == 404) {
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

    suspend fun testIsmJson(test: String, sToken: String, icm: String): String {
        return api.getTestIsm(
            GetTestIsmPostData(
                test = JSONObject(test).toString(),
                sToken = sToken,
                icm = JSONObject(icm).toString()
            )
        ).body()!!.string()
    }

    suspend fun remoteNonce(): RemoteNonce {
        return api.getNonce()
    }
}
