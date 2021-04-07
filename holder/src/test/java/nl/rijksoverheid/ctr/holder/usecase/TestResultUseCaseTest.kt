package nl.rijksoverheid.ctr.holder.usecase

import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.holder.*
import nl.rijksoverheid.ctr.holder.models.*
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
    fun `testResult returns InvalidToken if a uniquecode has 1 part`() = runBlocking {
        val usecase = TestResultUseCase(
            testProviderUseCase = fakeTestProviderUseCase(),
            testProviderRepository = fakeTestProviderRepository(),
            coronaCheckRepository = fakeCoronaCheckRepository(),
            commitmentMessageUseCase = fakeCommitmentMessageUsecase(),
            secretKeyUseCase = fakeSecretKeyUseCase(),
            createCredentialUseCase = fakeCreateCredentialUseCase(),
            personalDetailsUtil = fakePersonalDetailsUtil(),
            cachedAppConfigUseCase = fakeCachedAppConfigUseCase(),
            testResultUtil = fakeTestResultUtil(),
            tokenValidatorUtil = fakeTokenValidatorUtil()
        )
        val result = usecase.testResult(uniqueCode = "dummy")
        assertTrue(result is TestResult.InvalidToken)
    }

    @Test
    fun `testResult returns InvalidToken if a uniquecode has 2 parts`() = runBlocking {
        val usecase = TestResultUseCase(
            testProviderUseCase = fakeTestProviderUseCase(),
            testProviderRepository = fakeTestProviderRepository(),
            coronaCheckRepository = fakeCoronaCheckRepository(),
            commitmentMessageUseCase = fakeCommitmentMessageUsecase(),
            secretKeyUseCase = fakeSecretKeyUseCase(),
            createCredentialUseCase = fakeCreateCredentialUseCase(),
            personalDetailsUtil = fakePersonalDetailsUtil(),
            cachedAppConfigUseCase = fakeCachedAppConfigUseCase(),
            testResultUtil = fakeTestResultUtil(),
            tokenValidatorUtil = fakeTokenValidatorUtil()
        )
        val result = usecase.testResult(uniqueCode = "dummy-dummy")
        assertTrue(result is TestResult.InvalidToken)
    }

    @Test
    fun `testResult returns InvalidToken if token validator fails`() = runBlocking {
        val usecase = TestResultUseCase(
            testProviderUseCase = fakeTestProviderUseCase(),
            testProviderRepository = fakeTestProviderRepository(),
            coronaCheckRepository = fakeCoronaCheckRepository(),
            commitmentMessageUseCase = fakeCommitmentMessageUsecase(),
            secretKeyUseCase = fakeSecretKeyUseCase(),
            createCredentialUseCase = fakeCreateCredentialUseCase(),
            personalDetailsUtil = fakePersonalDetailsUtil(),
            cachedAppConfigUseCase = fakeCachedAppConfigUseCase(),
            testResultUtil = fakeTestResultUtil(),
            tokenValidatorUtil = fakeTokenValidatorUtil(
                isValid = false
            )
        )
        val result = usecase.testResult(uniqueCode = "provider-code-t1")
        assertTrue(result is TestResult.InvalidToken)
    }

    @Test
    fun `testResult returns InvalidToken if no provider matches`() = runBlocking {
        val usecase = TestResultUseCase(
            testProviderUseCase = fakeTestProviderUseCase(),
            testProviderRepository = fakeTestProviderRepository(),
            coronaCheckRepository = fakeCoronaCheckRepository(),
            commitmentMessageUseCase = fakeCommitmentMessageUsecase(),
            secretKeyUseCase = fakeSecretKeyUseCase(),
            createCredentialUseCase = fakeCreateCredentialUseCase(),
            personalDetailsUtil = fakePersonalDetailsUtil(),
            cachedAppConfigUseCase = fakeCachedAppConfigUseCase(),
            testResultUtil = fakeTestResultUtil(),
            tokenValidatorUtil = fakeTokenValidatorUtil()
        )
        val result = usecase.testResult(uniqueCode = "provider-code-t1")
        assertTrue(result is TestResult.InvalidToken)
    }

    @Test
    fun `testResult returns NegativeTestResult if RemoteTestResult status is Complete and test result is valid`() =
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
                    getRemoteTestResult(status = RemoteTestResult.Status.COMPLETE)
                ),
                coronaCheckRepository = fakeCoronaCheckRepository(),
                commitmentMessageUseCase = fakeCommitmentMessageUsecase(),
                secretKeyUseCase = fakeSecretKeyUseCase(),
                createCredentialUseCase = fakeCreateCredentialUseCase(),
                personalDetailsUtil = fakePersonalDetailsUtil(),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase(),
                testResultUtil = fakeTestResultUtil(),
                tokenValidatorUtil = fakeTokenValidatorUtil()
            )
            val result = usecase.testResult(uniqueCode = "$providerIdentifier-code-t1")
            assertTrue(result is TestResult.NegativeTestResult)
        }

    @Test
    fun `testResult returns NoNegativeTestResult if RemoteTestResult status is Complete and test result is not valid`() =
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
                    getRemoteTestResult(status = RemoteTestResult.Status.COMPLETE)
                ),
                coronaCheckRepository = fakeCoronaCheckRepository(),
                commitmentMessageUseCase = fakeCommitmentMessageUsecase(),
                secretKeyUseCase = fakeSecretKeyUseCase(),
                createCredentialUseCase = fakeCreateCredentialUseCase(),
                personalDetailsUtil = fakePersonalDetailsUtil(),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase(),
                testResultUtil = fakeTestResultUtil(isValid = false),
                tokenValidatorUtil = fakeTokenValidatorUtil()
            )
            val result = usecase.testResult(uniqueCode = "$providerIdentifier-code-t1")
            assertTrue(result is TestResult.NoNegativeTestResult)
        }

    @Test
    fun `testResult returns NoNegativeTestResult if RemoteTestResult status is Complete and test result is positive`() =
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
                    getRemoteTestResult(
                        status = RemoteTestResult.Status.COMPLETE,
                        negativeResult = false
                    )
                ),
                coronaCheckRepository = fakeCoronaCheckRepository(),
                commitmentMessageUseCase = fakeCommitmentMessageUsecase(),
                secretKeyUseCase = fakeSecretKeyUseCase(),
                createCredentialUseCase = fakeCreateCredentialUseCase(),
                personalDetailsUtil = fakePersonalDetailsUtil(),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase(),
                testResultUtil = fakeTestResultUtil(),
                tokenValidatorUtil = fakeTokenValidatorUtil()
            )
            val result = usecase.testResult(uniqueCode = "$providerIdentifier-code-t1")
            assertTrue(result is TestResult.NoNegativeTestResult)
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
                secretKeyUseCase = fakeSecretKeyUseCase(),
                createCredentialUseCase = fakeCreateCredentialUseCase(),
                personalDetailsUtil = fakePersonalDetailsUtil(),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase(),
                testResultUtil = fakeTestResultUtil(),
                tokenValidatorUtil = fakeTokenValidatorUtil()
            )
            val result = usecase.testResult(uniqueCode = "$providerIdentifier-code-t1")
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
                secretKeyUseCase = fakeSecretKeyUseCase(),
                createCredentialUseCase = fakeCreateCredentialUseCase(),
                personalDetailsUtil = fakePersonalDetailsUtil(),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase(),
                testResultUtil = fakeTestResultUtil(),
                tokenValidatorUtil = fakeTokenValidatorUtil()
            )
            val result = usecase.testResult(uniqueCode = "$providerIdentifier-code-t1")
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
                testProviderRepository = fakeTestProviderRepository(
                    remoteTestResultExceptionCallback = {
                        throw HttpException(
                            Response.error<String>(
                                400, "".toResponseBody()
                            )
                        )
                    }),
                coronaCheckRepository = fakeCoronaCheckRepository(),
                commitmentMessageUseCase = fakeCommitmentMessageUsecase(),
                secretKeyUseCase = fakeSecretKeyUseCase(),
                createCredentialUseCase = fakeCreateCredentialUseCase(),
                personalDetailsUtil = fakePersonalDetailsUtil(),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase(),
                testResultUtil = fakeTestResultUtil(),
                tokenValidatorUtil = fakeTokenValidatorUtil()
            )
            val result = usecase.testResult(uniqueCode = "$providerIdentifier-code-t1")
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
                secretKeyUseCase = fakeSecretKeyUseCase(),
                createCredentialUseCase = fakeCreateCredentialUseCase(),
                personalDetailsUtil = fakePersonalDetailsUtil(),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase(),
                testResultUtil = fakeTestResultUtil(),
                tokenValidatorUtil = fakeTokenValidatorUtil()
            )
            val result = usecase.testResult(uniqueCode = "$providerIdentifier-code-t1")
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
                secretKeyUseCase = fakeSecretKeyUseCase(),
                createCredentialUseCase = fakeCreateCredentialUseCase(),
                personalDetailsUtil = fakePersonalDetailsUtil(),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase(),
                testResultUtil = fakeTestResultUtil(),
                tokenValidatorUtil = fakeTokenValidatorUtil()
            )
            val result = usecase.testResult(uniqueCode = "$providerIdentifier-code-t1")
            assertTrue(result is TestResult.Pending)
        }

    @Test
    fun `signTestResult returns Complete if TestIsmResult is Success`() = runBlocking {
        runBlocking {
            val usecase = TestResultUseCase(
                testProviderUseCase = fakeTestProviderUseCase(),
                testProviderRepository = fakeTestProviderRepository(),
                coronaCheckRepository = fakeCoronaCheckRepository(
                    testIsmResult = TestIsmResult.Success(
                        body = "dummy"
                    )
                ),
                commitmentMessageUseCase = fakeCommitmentMessageUsecase(),
                secretKeyUseCase = fakeSecretKeyUseCase(),
                createCredentialUseCase = fakeCreateCredentialUseCase(),
                personalDetailsUtil = fakePersonalDetailsUtil(),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase(),
                testResultUtil = fakeTestResultUtil(),
                tokenValidatorUtil = fakeTokenValidatorUtil()
            )
            val result = usecase.signTestResult(getRemoteTestResult())
            assertTrue(result is SignedTestResult.Complete)
        }
    }

    @Test
    fun `signTestResult returns AlreadySigned if TestIsmResult is Error with CODE_ALREADY_SIGNED error`() =
        runBlocking {
            runBlocking {
                val providerIdentifier = "provider"
                val usecase = TestResultUseCase(
                    testProviderUseCase = fakeTestProviderUseCase(
                        provider = getRemoteTestProvider(
                            identifier = providerIdentifier
                        )
                    ),
                    testProviderRepository = fakeTestProviderRepository(),
                    coronaCheckRepository = fakeCoronaCheckRepository(
                        testIsmResult = TestIsmResult.Error(
                            responseError = ResponseError(
                                status = "dummy",
                                code = ResponseError.CODE_ALREADY_SIGNED
                            ),
                            httpCode = 400
                        )
                    ),
                    commitmentMessageUseCase = fakeCommitmentMessageUsecase(),
                    secretKeyUseCase = fakeSecretKeyUseCase(),
                    createCredentialUseCase = fakeCreateCredentialUseCase(),
                    personalDetailsUtil = fakePersonalDetailsUtil(),
                    cachedAppConfigUseCase = fakeCachedAppConfigUseCase(),
                    testResultUtil = fakeTestResultUtil(),
                    tokenValidatorUtil = fakeTokenValidatorUtil()
                )
                val result = usecase.signTestResult(getRemoteTestResult())
                assertTrue(result is SignedTestResult.AlreadySigned)
            }
        }

    @Test
    fun `signTestResult returns ServerError if TestIsmResult is Error`() = runBlocking {
        runBlocking {
            val providerIdentifier = "provider"
            val usecase = TestResultUseCase(
                testProviderUseCase = fakeTestProviderUseCase(
                    provider = getRemoteTestProvider(
                        identifier = providerIdentifier
                    )
                ),
                testProviderRepository = fakeTestProviderRepository(),
                coronaCheckRepository = fakeCoronaCheckRepository(
                    testIsmResult = TestIsmResult.Error(
                        responseError = ResponseError(
                            status = "dummy",
                            code = 0
                        ),
                        httpCode = 400
                    )
                ),
                commitmentMessageUseCase = fakeCommitmentMessageUsecase(),
                secretKeyUseCase = fakeSecretKeyUseCase(),
                createCredentialUseCase = fakeCreateCredentialUseCase(),
                personalDetailsUtil = fakePersonalDetailsUtil(),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase(),
                testResultUtil = fakeTestResultUtil(),
                tokenValidatorUtil = fakeTokenValidatorUtil()
            )
            val result = usecase.signTestResult(getRemoteTestResult())
            assertTrue(result is SignedTestResult.ServerError)
        }
    }

    @Test
    fun `signTestResult returns ServerError if HttpException is thrown`() = runBlocking {
        runBlocking {
            val providerIdentifier = "provider"
            val usecase = TestResultUseCase(
                testProviderUseCase = fakeTestProviderUseCase(
                    provider = getRemoteTestProvider(
                        identifier = providerIdentifier
                    )
                ),
                testProviderRepository = fakeTestProviderRepository(),
                coronaCheckRepository = fakeCoronaCheckRepository(testIsmExceptionCallback = {
                    throw HttpException(
                        Response.error<String>(
                            400, "".toResponseBody()
                        )
                    )
                }),
                commitmentMessageUseCase = fakeCommitmentMessageUsecase(),
                secretKeyUseCase = fakeSecretKeyUseCase(),
                createCredentialUseCase = fakeCreateCredentialUseCase(),
                personalDetailsUtil = fakePersonalDetailsUtil(),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase(),
                testResultUtil = fakeTestResultUtil(),
                tokenValidatorUtil = fakeTokenValidatorUtil()
            )
            val result = usecase.signTestResult(getRemoteTestResult())
            assertTrue(result is SignedTestResult.ServerError)
        }
    }

    @Test
    fun `signTestResult returns NetworkError if IOException is thrown`() = runBlocking {
        runBlocking {
            val providerIdentifier = "provider"
            val usecase = TestResultUseCase(
                testProviderUseCase = fakeTestProviderUseCase(
                    provider = getRemoteTestProvider(
                        identifier = providerIdentifier
                    )
                ),
                testProviderRepository = fakeTestProviderRepository(),
                coronaCheckRepository = fakeCoronaCheckRepository(testIsmExceptionCallback = {
                    throw IOException()
                }),
                commitmentMessageUseCase = fakeCommitmentMessageUsecase(),
                secretKeyUseCase = fakeSecretKeyUseCase(),
                createCredentialUseCase = fakeCreateCredentialUseCase(),
                personalDetailsUtil = fakePersonalDetailsUtil(),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase(),
                testResultUtil = fakeTestResultUtil(),
                tokenValidatorUtil = fakeTokenValidatorUtil()
            )
            val result = usecase.signTestResult(getRemoteTestResult())
            assertTrue(result is SignedTestResult.NetworkError)
        }
    }

    private fun getRemoteTestProvider(identifier: String): RemoteTestProviders.Provider {
        return RemoteTestProviders.Provider(
            name = "dummy",
            providerIdentifier = identifier,
            resultUrl = "dummy",
            publicKey = "dummy".toByteArray()
        )
    }

    private fun getRemoteTestResult(
        status: RemoteTestResult.Status = RemoteTestResult.Status.COMPLETE,
        negativeResult: Boolean = true
    ): SignedResponseWithModel<RemoteTestResult> {
        return SignedResponseWithModel(
            rawResponse = "dummy".toByteArray(), model = RemoteTestResult(
                result = RemoteTestResult.Result(
                    unique = "dummy",
                    sampleDate = OffsetDateTime.now(),
                    testType = "dummy",
                    negativeResult = negativeResult,
                    holder = Holder(
                        firstNameInitial = "A",
                        lastNameInitial = "B",
                        birthDay = "1",
                        birthMonth = "2"
                    )
                ),
                providerIdentifier = "dummy",
                status = status,
                protocolVersion = "dummy"
            )
        )
    }
}
