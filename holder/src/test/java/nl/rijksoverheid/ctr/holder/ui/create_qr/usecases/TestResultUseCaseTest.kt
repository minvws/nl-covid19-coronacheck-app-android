package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.holder.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.TestProviderRepository
import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult
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
            configProviderUseCase = fakeConfigProviderUseCase(),
            testProviderRepository = fakeTestProviderRepository(),
            tokenValidatorUtil = fakeTokenValidatorUtil(),
            configUseCase = fakeCachedAppConfigUseCase(),
        )
        val result = usecase.testResult(uniqueCode = "dummy")
        assertTrue(result is TestResult.InvalidToken)
    }

    @Test
    fun `testResult returns InvalidToken if a uniquecode has 2 parts`() = runBlocking {
        val usecase = TestResultUseCase(
            configProviderUseCase = fakeConfigProviderUseCase(),
            testProviderRepository = fakeTestProviderRepository(),
            tokenValidatorUtil = fakeTokenValidatorUtil(),
            configUseCase = fakeCachedAppConfigUseCase(),
        )
        val result = usecase.testResult(uniqueCode = "dummy-dummy")
        assertTrue(result is TestResult.InvalidToken)
    }

    @Test
    fun `testResult returns InvalidToken if token validator fails`() = runBlocking {
        val usecase = TestResultUseCase(
            configProviderUseCase = fakeConfigProviderUseCase(),
            testProviderRepository = fakeTestProviderRepository(),
            tokenValidatorUtil = fakeTokenValidatorUtil(
                isValid = false
            ),
            configUseCase = fakeCachedAppConfigUseCase(),
        )
        val result = usecase.testResult(uniqueCode = "provider-B-t1")
        assertTrue(result is TestResult.InvalidToken)
    }

    @Test
    fun `testResult returns InvalidToken if no provider matches`() = runBlocking {
        val usecase = TestResultUseCase(
            configProviderUseCase = fakeConfigProviderUseCase(),
            testProviderRepository = fakeTestProviderRepository(),
            tokenValidatorUtil = fakeTokenValidatorUtil(),
            configUseCase = fakeCachedAppConfigUseCase(),
        )
        val result = usecase.testResult(uniqueCode = "provider-B-t1")
        assertTrue(result is TestResult.InvalidToken)
    }

    @Test
    fun `testResult returns NegativeTestResult if RemoteTestResult status is Complete and test result is valid`() =
        runBlocking {
            val providerIdentifier = "provider"
            val usecase = TestResultUseCase(
                configProviderUseCase = fakeConfigProviderUseCase(
                    testProviders = listOf(getRemoteTestProvider(
                        identifier = providerIdentifier
                    ))
                ),
                testProviderRepository = fakeTestProviderRepository(
                    model =
                    getRemoteTestResult(status = RemoteProtocol.Status.COMPLETE)
                ),
                tokenValidatorUtil = fakeTokenValidatorUtil(),
                configUseCase = fakeCachedAppConfigUseCase(),
            )
            val result = usecase.testResult(uniqueCode = "$providerIdentifier-B-t1")
            assertTrue(result is TestResult.NegativeTestResult)
        }

    @Test
    fun `testResult returns NoNegativeTestResult if RemoteTestResult status is Complete and test result is positive`() =
        runBlocking {
            val providerIdentifier = "provider"
            val usecase = TestResultUseCase(
                configProviderUseCase = fakeConfigProviderUseCase(
                    testProviders = listOf(getRemoteTestProvider(
                        identifier = providerIdentifier
                    )
                )),
                testProviderRepository = fakeTestProviderRepository(
                    model = getRemoteTestResult(
                        status = RemoteProtocol.Status.COMPLETE,
                        negativeResult = false
                    )
                ),
                tokenValidatorUtil = fakeTokenValidatorUtil(),
                configUseCase = fakeCachedAppConfigUseCase(),
            )
            val result = usecase.testResult(uniqueCode = "$providerIdentifier-B-t1")
            assertTrue(result is TestResult.NoNegativeTestResult)
        }

    @Test
    fun `testResult returns VerificationRequired if RemoteTestResult status is VerificationRequired`() =
        runBlocking {
            val providerIdentifier = "provider"
            val usecase = TestResultUseCase(
                configProviderUseCase = fakeConfigProviderUseCase(
                    testProviders = listOf(getRemoteTestProvider(
                        identifier = providerIdentifier
                    )
                )),
                testProviderRepository = fakeTestProviderRepository(
                    model =
                    getRemoteTestResult(status = RemoteProtocol.Status.VERIFICATION_REQUIRED)
                ),
                tokenValidatorUtil = fakeTokenValidatorUtil(),
                configUseCase = fakeCachedAppConfigUseCase(),
            )
            val result = usecase.testResult(uniqueCode = "$providerIdentifier-B-t1")
            assertTrue(result is TestResult.VerificationRequired)
        }

    @Test
    fun `testResult returns InvalidToken if RemoteTestResult status is InvalidToken`() =
        runBlocking {
            val providerIdentifier = "provider"
            val usecase = TestResultUseCase(
                configProviderUseCase = fakeConfigProviderUseCase(
                    testProviders = listOf(getRemoteTestProvider(
                        identifier = providerIdentifier
                    )
                )),
                testProviderRepository = fakeTestProviderRepository(
                    model =
                    getRemoteTestResult(status = RemoteProtocol.Status.INVALID_TOKEN)
                ),
                tokenValidatorUtil = fakeTokenValidatorUtil(),
                configUseCase = fakeCachedAppConfigUseCase(),
            )
            val result = usecase.testResult(uniqueCode = "$providerIdentifier-B-t1")
            assertTrue(result is TestResult.InvalidToken)
        }

    @Test
    fun `testResult returns ServerError if HttpException is thrown`() =
        runBlocking {
            val providerIdentifier = "provider"
            val usecase = TestResultUseCase(
                configProviderUseCase = fakeConfigProviderUseCase(
                    testProviders = listOf(getRemoteTestProvider(
                        identifier = providerIdentifier
                    )
                )),
                testProviderRepository = fakeTestProviderRepository(
                    remoteTestResultExceptionCallback = {
                        throw HttpException(
                            Response.error<String>(
                                400, "".toResponseBody()
                            )
                        )
                    }),
                tokenValidatorUtil = fakeTokenValidatorUtil(),
                configUseCase = fakeCachedAppConfigUseCase(),
            )
            val result = usecase.testResult(uniqueCode = "$providerIdentifier-B-t1")
            assertTrue(result is TestResult.Error)
        }

    @Test
    fun `testResult returns NetworkError if IOException is thrown`() =
        runBlocking {
            val providerIdentifier = "provider"
            val usecase = TestResultUseCase(
                configProviderUseCase = fakeConfigProviderUseCase(
                    testProviders = listOf(getRemoteTestProvider(
                        identifier = providerIdentifier
                    )
                )),
                testProviderRepository = object : TestProviderRepository {
                    override suspend fun remoteTestResult(
                        url: String,
                        token: String,
                        provider: String,
                        verifierCode: String?,
                        signingCertificateBytes: ByteArray
                    ): NetworkRequestResult<SignedResponseWithModel<RemoteProtocol>> {
                        throw IOException()
                    }
                },
                tokenValidatorUtil = fakeTokenValidatorUtil(),
                configUseCase = fakeCachedAppConfigUseCase(),
            )
            val result = usecase.testResult(uniqueCode = "$providerIdentifier-B-t1")
            assertTrue(result is TestResult.Error)
        }

    @Test
    fun `testResult returns Pending if RemoteTestResult status is Pending`() =
        runBlocking {
            val providerIdentifier = "provider"
            val usecase = TestResultUseCase(
                configProviderUseCase = fakeConfigProviderUseCase(
                    testProviders = listOf(getRemoteTestProvider(
                        identifier = providerIdentifier
                    )
                )),
                testProviderRepository = fakeTestProviderRepository(
                    model =
                    getRemoteTestResult(status = RemoteProtocol.Status.PENDING)
                ),
                tokenValidatorUtil = fakeTokenValidatorUtil(),
                configUseCase = fakeCachedAppConfigUseCase(),
            )
            val result = usecase.testResult(uniqueCode = "$providerIdentifier-B-t1")
            assertTrue(result is TestResult.Pending)
        }

    private fun getRemoteTestProvider(identifier: String): RemoteConfigProviders.TestProvider {
        return RemoteConfigProviders.TestProvider(
            name = "dummy",
            providerIdentifier = identifier,
            resultUrl = "dummy",
            publicKey = "dummy".toByteArray()
        )
    }

    private fun getRemoteTestResult(
        status: RemoteProtocol.Status = RemoteProtocol.Status.COMPLETE,
        negativeResult: Boolean = true
    ): SignedResponseWithModel<RemoteProtocol> {
        return SignedResponseWithModel(
            rawResponse = "dummy".toByteArray(), model = RemoteTestResult2(
                result = RemoteTestResult2.Result(
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
