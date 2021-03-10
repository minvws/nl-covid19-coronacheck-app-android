package nl.rijksoverheid.ctr.holder.usecase

import nl.rijksoverheid.ctr.holder.models.RemoteTestResult
import nl.rijksoverheid.ctr.holder.models.ResponseError
import nl.rijksoverheid.ctr.holder.models.SignedResponseWithModel
import nl.rijksoverheid.ctr.holder.models.TestIsmResult
import nl.rijksoverheid.ctr.holder.repositories.CoronaCheckRepository
import nl.rijksoverheid.ctr.holder.repositories.TestProviderRepository
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class TestResultUseCase(
    private val testProviderUseCase: TestProviderUseCase,
    private val testProviderRepository: TestProviderRepository,
    private val coronaCheckRepository: CoronaCheckRepository,
    private val commitmentMessageUseCase: CommitmentMessageUseCase,
    private val secretKeyUseCase: SecretKeyUseCase,
    private val createCredentialUseCase: CreateCredentialUseCase
) {

    suspend fun testResult(uniqueCode: String, verificationCode: String? = null): TestResult {
        if (uniqueCode.indexOf("-") == -1) {
            return TestResult.InvalidToken
        }

        val uniqueCodeAttributes = uniqueCode.split("-")
        val providerIdentifier = uniqueCodeAttributes[0]
        val token = uniqueCodeAttributes[1]

        return try {
            val testProvider = testProviderUseCase.testProvider(providerIdentifier)
                ?: return TestResult.InvalidToken

            val signedResponseWithTestResult = testProviderRepository.remoteTestResult(
                url = testProvider.resultUrl,
                token = token,
                verifierCode = verificationCode,
                signingCertificateBytes = testProvider.publicKey
            )

            val remoteTestResult = signedResponseWithTestResult.model

            when (remoteTestResult.status) {
                RemoteTestResult.Status.VERIFICATION_REQUIRED -> return TestResult.VerificationRequired
                RemoteTestResult.Status.INVALID_TOKEN -> return TestResult.InvalidToken
                RemoteTestResult.Status.PENDING -> return TestResult.Pending
                RemoteTestResult.Status.COMPLETE -> {
                    // nothing
                }
                else -> throw IllegalStateException("Unsupported status ${remoteTestResult.status}")
            }

            val result = remoteTestResult.result ?: error("Expected result")
            TestResult.Complete(remoteTestResult, signedResponseWithTestResult)
        } catch (ex: HttpException) {
            Timber.e(ex, "Server error while getting test result")
            TestResult.ServerError
        } catch (ex: IOException) {
            Timber.e(ex, "Network error while getting test result")
            TestResult.NetworkError
        }
    }

    suspend fun signTestResult(
        signedResponseWithTestResult: SignedResponseWithModel<RemoteTestResult>
    ): SignedTestResult {
        try {
            // Persist encrypted test result
            val remoteNonce = coronaCheckRepository.remoteNonce()
            val commitmentMessage = commitmentMessageUseCase.json(
                nonce = remoteNonce.nonce
            )
            Timber.i("Received commitment message $commitmentMessage")

            val testIsm = coronaCheckRepository.getTestIsm(
                test = signedResponseWithTestResult.rawResponse.toString(Charsets.UTF_8),
                sToken = remoteNonce.sToken,
                icm = commitmentMessage
            )
            when (testIsm) {
                is TestIsmResult.Success -> {
                    Timber.i("Received test ism json ${testIsm.body}")

                    val credential = createCredentialUseCase.get(
                        secretKeyJson = secretKeyUseCase.json(),
                        testIsmBody = testIsm.body
                    )

                    return SignedTestResult.Complete(credential)
                }
                is TestIsmResult.Error -> {
                    return if (testIsm.responseError.code == ResponseError.CODE_ALREADY_SIGNED) {
                        SignedTestResult.AlreadySigned
                    } else {
                        SignedTestResult.ServerError
                    }
                }
            }
        } catch (ex: HttpException) {
            return SignedTestResult.ServerError
        } catch (ex: IOException) {
            return SignedTestResult.NetworkError
        }
    }
}

sealed class TestResult {
    data class Complete(
        val remoteTestResult: RemoteTestResult,
        val signedResponseWithTestResult: SignedResponseWithModel<RemoteTestResult>
    ) :
        TestResult()

    object Pending : TestResult()
    object InvalidToken : TestResult()
    object VerificationRequired : TestResult()
    object ServerError : TestResult()
    object NetworkError : TestResult()
}

sealed class SignedTestResult {
    data class Complete(val credentials: String) : SignedTestResult()
    object AlreadySigned : SignedTestResult()
    object ServerError : SignedTestResult()
    object NetworkError : SignedTestResult()
}
