package nl.rijksoverheid.ctr.holder.ui.create_qr.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.api.factory.NetworkRequestResultFactory
import nl.rijksoverheid.ctr.api.interceptors.SigningCertificate
import nl.rijksoverheid.ctr.holder.HolderStep
import nl.rijksoverheid.ctr.holder.ui.create_qr.api.TestProviderApiClient
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteProtocol
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.SignedResponseWithModel
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.post.GetTestResultPostData
import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.HttpException

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface TestProviderRepository {
    suspend fun remoteTestResult(
        url: String,
        token: String,
        provider: String,
        verifierCode: String?,
        signingCertificateBytes: ByteArray
    ): NetworkRequestResult<SignedResponseWithModel<RemoteProtocol>>
}

class TestProviderRepositoryImpl(
    private val testProviderApiClient: TestProviderApiClient,
    private val networkRequestResultFactory: NetworkRequestResultFactory,
    private val responseConverter: Converter<ResponseBody, SignedResponseWithModel<RemoteProtocol>>,
) : TestProviderRepository {
    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun remoteTestResult(
        url: String,
        token: String,
        provider: String,
        verifierCode: String?,
        signingCertificateBytes: ByteArray
    ): NetworkRequestResult<SignedResponseWithModel<RemoteProtocol>> {
        return networkRequestResultFactory.createResult(
            step = HolderStep.TestResultNetworkRequest,
            provider = provider,
            networkCall = {
                testProviderApiClient.getTestResult(
                    url = url,
                    authorization = "Bearer $token",
                    data = verifierCode?.let {
                        GetTestResultPostData(
                            it
                        )
                    },
                    certificate = SigningCertificate(signingCertificateBytes)
                )
            },
            interceptHttpError = {
                val errorBody = it.response()?.errorBody()
                if (it.code() == 401) {
                    withContext(Dispatchers.IO) {
                        responseConverter.convert(errorBody)
                    }
                } else {
                    null
                }
            }
        )
    }
}
