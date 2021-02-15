package nl.rijksoverheid.ctr.holder.usecase

import clmobile.Clmobile
import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.repositories.HolderRepository
import nl.rijksoverheid.ctr.shared.ext.successString
import nl.rijksoverheid.ctr.shared.models.RemoteTestResult
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
    private val holderRepository: HolderRepository,
    private val moshi: Moshi,
    private val commitmentMessageUseCase: CommitmentMessageUseCase,
    private val secretKeyUseCase: SecretKeyUseCase,
    private val persistenceManager: PersistenceManager
) {

    suspend fun testResult(uniqueCode: String, verificationCode: String?): TestResult {
        if (uniqueCode.indexOf("-") == -1) {
            return TestResult.InvalidToken
        }

        val uniqueCodeAttributes = uniqueCode.split("-")
        val providerIdentifier = uniqueCodeAttributes[0]
        val token = uniqueCodeAttributes[1]

        val testProvider = testProviderUseCase.testProvider(providerIdentifier)
            ?: return TestResult.InvalidToken

        return try {
            val signedResponseWithTestResult = holderRepository.remoteTestResult(
                url = testProvider.resultUrl,
                token = token,
                verifierCode = verificationCode,
                signingCertificateBytes = testProvider.publicKey
            )

            val remoteTestResult = signedResponseWithTestResult.model

            when (remoteTestResult.status) {
                RemoteTestResult.Status.VERIFICATION_REQUIRED -> return TestResult.VerificationRequired
                RemoteTestResult.Status.INVALID_TOKEN -> return TestResult.InvalidToken
                RemoteTestResult.Status.COMPLETE -> {
                    // nothing
                }
                else -> throw IllegalStateException("Unsupported status ${remoteTestResult.status}")
            }

            val result = remoteTestResult.result ?: error("Expected result")

            // Persist encrypted test result
            val remoteNonce = holderRepository.remoteNonce()
            val commitmentMessage = commitmentMessageUseCase.json(
                nonce = remoteNonce.nonce
            )
            Timber.i("Received commitment message $commitmentMessage")

            val testIsmJson = holderRepository.testIsmJson(
                test = signedResponseWithTestResult.payload.toString(Charsets.UTF_8),
                sToken = remoteNonce.sToken,
                icm = commitmentMessage
            )
            Timber.i("Received test ism json $testIsmJson")

            val credentials = Clmobile.createCredential(
                secretKeyUseCase.json().toByteArray(Charsets.UTF_8),
                testIsmJson.toByteArray(Charsets.UTF_8)
            ).successString()

            persistenceManager.saveCredentials(credentials)
            TestResult.Success(remoteTestResult)
        } catch (ex: HttpException) {
            Timber.e(ex, "Server error while getting test result")
            TestResult.ServerError
        } catch (ex: IOException) {
            Timber.e(ex, "Network error while getting test result")
            TestResult.NetworkError
        }
    }
}

sealed class TestResult {
    data class Success(val remoteTestResult: RemoteTestResult) : TestResult()
    object InvalidToken : TestResult()
    object VerificationRequired : TestResult()
    object ServerError : TestResult()
    object NetworkError : TestResult()
}
