package nl.rijksoverheid.ctr.holder.usecase

import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.api.models.RemoteTestProviders
import nl.rijksoverheid.ctr.api.models.RemoteTestResult
import nl.rijksoverheid.ctr.api.models.SignedResponseWithModel
import nl.rijksoverheid.ctr.holder.*
import nl.rijksoverheid.ctr.holder.repositories.TestProviderRepository
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.time.OffsetDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class TestResultUseCaseTest {

    @Test
    fun `testResult returns InvalidToken if a uniquecode does not contain -`() = runBlocking {
        val usecase = TestResultUseCase(
            testProviderUseCase = fakeTestProviderUseCase(),
            testProviderRepository = fakeTestProviderRepository(),
            coronaCheckRepository = fakeCoronaCheckRepository(),
            commitmentMessageUseCase = fakeCommitmentMessageUsecase(),
            secretKeyUseCase = fakeSecretKeyUseCase()
        )
        val result = usecase.testResult(uniqueCode = "dummy")
        assertTrue(result is TestResult.InvalidToken)
    }

    @Test
    fun `testResult returns InvalidToken if no provider matches`() = runBlocking {
        val usecase = TestResultUseCase(
            testProviderUseCase = fakeTestProviderUseCase(),
            testProviderRepository = fakeTestProviderRepository(),
            coronaCheckRepository = fakeCoronaCheckRepository(),
            commitmentMessageUseCase = fakeCommitmentMessageUsecase(),
            secretKeyUseCase = fakeSecretKeyUseCase()
        )
        val result = usecase.testResult(uniqueCode = "provider-code")
        assertTrue(result is TestResult.InvalidToken)
    }

    @Test
    fun `testResult returns Complete if RemoteTestResult status is Complete`() = runBlocking {
        val providerIdentifier = "provider"
        val usecase = TestResultUseCase(
            testProviderUseCase = fakeTestProviderUseCase(
                provider = getRemoteTestProvider(
                    identifier = providerIdentifier
                )
            ),
            testProviderRepository = fakeTestProviderRepository(
                model =
                getRemoteTestResult(status = RemoteTestResult.Status.COMPLETE)
            ),
            coronaCheckRepository = fakeCoronaCheckRepository(),
            commitmentMessageUseCase = fakeCommitmentMessageUsecase(),
            secretKeyUseCase = fakeSecretKeyUseCase()
        )
        val result = usecase.testResult(uniqueCode = "$providerIdentifier-code")
        assertTrue(result is TestResult.Complete)
    }

    @Test
    fun `testResult returns VerificationRequired if RemoteTestResult status is VerificationRequired`() =
        runBlocking {
            val providerIdentifier = "provider"
            val usecase = TestResultUseCase(
                testProviderUseCase = fakeTestProviderUseCase(
                    provider = getRemoteTestProvider(
                        identifier = providerIdentifier
                    )
                ),
                testProviderRepository = fakeTestProviderRepository(
                    model =
                    getRemoteTestResult(status = RemoteTestResult.Status.VERIFICATION_REQUIRED)
                ),
                coronaCheckRepository = fakeCoronaCheckRepository(),
                commitmentMessageUseCase = fakeCommitmentMessageUsecase(),
                secretKeyUseCase = fakeSecretKeyUseCase()
            )
            val result = usecase.testResult(uniqueCode = "$providerIdentifier-code")
            assertTrue(result is TestResult.VerificationRequired)
        }

    @Test
    fun `testResult returns InvalidToken if RemoteTestResult status is InvalidToken`() =
        runBlocking {
            val providerIdentifier = "provider"
            val usecase = TestResultUseCase(
                testProviderUseCase = fakeTestProviderUseCase(
                    provider = getRemoteTestProvider(
                        identifier = providerIdentifier
                    )
                ),
                testProviderRepository = fakeTestProviderRepository(
                    model =
                    getRemoteTestResult(status = RemoteTestResult.Status.INVALID_TOKEN)
                ),
                coronaCheckRepository = fakeCoronaCheckRepository(),
                commitmentMessageUseCase = fakeCommitmentMessageUsecase(),
                secretKeyUseCase = fakeSecretKeyUseCase()
            )
            val result = usecase.testResult(uniqueCode = "$providerIdentifier-code")
            assertTrue(result is TestResult.InvalidToken)
        }

    @Test
    fun `testResult returns ServerError if HttpException is thrown`() =
        runBlocking {
            val providerIdentifier = "provider"
            val usecase = TestResultUseCase(
                testProviderUseCase = fakeTestProviderUseCase(
                    provider = getRemoteTestProvider(
                        identifier = providerIdentifier
                    )
                ),
                testProviderRepository = object : TestProviderRepository {
                    override suspend fun remoteTestResult(
                        url: String,
                        token: String,
                        verifierCode: String?,
                        signingCertificateBytes: ByteArray
                    ): SignedResponseWithModel<RemoteTestResult> {
                        throw HttpException(
                            Response.error<String>(
                                400, "".toResponseBody()
                            )
                        )
                    }
                },
                coronaCheckRepository = fakeCoronaCheckRepository(),
                commitmentMessageUseCase = fakeCommitmentMessageUsecase(),
                secretKeyUseCase = fakeSecretKeyUseCase()
            )
            val result = usecase.testResult(uniqueCode = "$providerIdentifier-code")
            assertTrue(result is TestResult.ServerError)
        }

    @Test
    fun `testResult returns NetworkError if IOException is thrown`() =
        runBlocking {
            val providerIdentifier = "provider"
            val usecase = TestResultUseCase(
                testProviderUseCase = fakeTestProviderUseCase(
                    provider = getRemoteTestProvider(
                        identifier = providerIdentifier
                    )
                ),
                testProviderRepository = object : TestProviderRepository {
                    override suspend fun remoteTestResult(
                        url: String,
                        token: String,
                        verifierCode: String?,
                        signingCertificateBytes: ByteArray
                    ): SignedResponseWithModel<RemoteTestResult> {
                        throw IOException()
                    }
                },
                coronaCheckRepository = fakeCoronaCheckRepository(),
                commitmentMessageUseCase = fakeCommitmentMessageUsecase(),
                secretKeyUseCase = fakeSecretKeyUseCase()
            )
            val result = usecase.testResult(uniqueCode = "$providerIdentifier-code")
            assertTrue(result is TestResult.NetworkError)
        }

    @Test
    fun `testResult returns Pending if RemoteTestResult status is Pending`() =
        runBlocking {
            val providerIdentifier = "provider"
            val usecase = TestResultUseCase(
                testProviderUseCase = fakeTestProviderUseCase(
                    provider = getRemoteTestProvider(
                        identifier = providerIdentifier
                    )
                ),
                testProviderRepository = fakeTestProviderRepository(
                    model =
                    getRemoteTestResult(status = RemoteTestResult.Status.PENDING)
                ),
                coronaCheckRepository = fakeCoronaCheckRepository(),
                commitmentMessageUseCase = fakeCommitmentMessageUsecase(),
                secretKeyUseCase = fakeSecretKeyUseCase()
            )
            val result = usecase.testResult(uniqueCode = "$providerIdentifier-code")
            assertTrue(result is TestResult.Pending)
        }

    private fun getRemoteTestProvider(identifier: String): RemoteTestProviders.Provider {
        return RemoteTestProviders.Provider(
            name = "dummy",
            providerIdentifier = identifier,
            resultUrl = "dummy",
            publicKey = "dummy".toByteArray()
        )
    }

    private fun getRemoteTestResult(status: RemoteTestResult.Status): SignedResponseWithModel<RemoteTestResult> {
        return SignedResponseWithModel(
            rawResponse = "dummy".toByteArray(), model = RemoteTestResult(
                result = RemoteTestResult.Result(
                    unique = "dummy",
                    sampleDate = OffsetDateTime.now(),
                    testType = "dummy",
                    negativeResult = false
                ),
                providerIdentifier = "dummy",
                status = status,
                protocolVersion = "dummy"
            )
        )
    }

}
