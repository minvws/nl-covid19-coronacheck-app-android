package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.HolderStep
import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteProtocol
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteTestResult2
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.SignedResponseWithModel
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.TestProviderRepository
import nl.rijksoverheid.ctr.holder.ui.myoverview.utils.TokenValidatorUtil
import nl.rijksoverheid.ctr.holder.ui.myoverview.utils.TokenValidatorUtilImpl
import nl.rijksoverheid.ctr.shared.ext.removeWhitespace
import nl.rijksoverheid.ctr.shared.models.AppErrorResult
import nl.rijksoverheid.ctr.shared.models.ErrorResult
import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class TestResultUseCase(
    private val configProviderUseCase: ConfigProvidersUseCase,
    private val testProviderRepository: TestProviderRepository,
    private val tokenValidatorUtil: TokenValidatorUtil,
    private val configUseCase: CachedAppConfigUseCase
) {

    suspend fun testResult(uniqueCode: String, verificationCode: String? = null): TestResult {
        try {
            if (uniqueCode.isEmpty()) {
                return TestResult.EmptyToken
            }

            if (uniqueCode.indexOf("-") == -1) {
                return TestResult.InvalidToken
            }

            val uniqueCodeAttributes = uniqueCode.split("-")

            if (uniqueCodeAttributes.size != 3) {
                return TestResult.InvalidToken
            }

            val providerIdentifier = uniqueCodeAttributes[0]
            val token = uniqueCodeAttributes[1]
            val checksum = uniqueCodeAttributes[2]

            // We need to check for valid chars
            token.toCharArray().forEach {
                if (!TokenValidatorUtilImpl.CODE_POINTS.contains(it)) {
                    return TestResult.InvalidToken
                }
            }

            // Enable the luhn check based on the current config value
            if (configUseCase.getCachedAppConfig().luhnCheckEnabled) {
                if (!tokenValidatorUtil.validate(token = token, checksum = checksum)) {
                    return TestResult.InvalidToken
                }
            }

            val testProvider =
                when (val testProvidersResult = configProviderUseCase.testProviders()) {
                    is TestProvidersResult.Success -> {
                        testProvidersResult.testProviders
                            .firstOrNull { it.providerIdentifier == providerIdentifier }
                            ?: return TestResult.UnknownTestProvider
                    }
                    is TestProvidersResult.Error -> {
                        return TestResult.Error(testProvidersResult.errorResult)
                    }
                }

            if (verificationCode != null && verificationCode.isEmpty()) {
                return TestResult.EmptyVerificationCode
            }

            val signedResponseWithTestResultRequestResult = testProviderRepository.remoteTestResult(
                url = testProvider.resultUrl,
                token = token.removeWhitespace(),
                verifierCode = verificationCode?.removeWhitespace() ?: "",
                signingCertificateBytes = testProvider.publicKey,
                provider = providerIdentifier
            )

            val signedResponseWithTestResult = when (signedResponseWithTestResultRequestResult) {
                is NetworkRequestResult.Success -> {
                    signedResponseWithTestResultRequestResult.response
                }
                is NetworkRequestResult.Failed -> {
                    return TestResult.Error(signedResponseWithTestResultRequestResult)
                }
            }

            val remoteTestResult = signedResponseWithTestResult.model

            when (remoteTestResult.status) {
                RemoteProtocol.Status.VERIFICATION_REQUIRED -> return TestResult.VerificationRequired
                RemoteProtocol.Status.INVALID_TOKEN -> return TestResult.InvalidToken
                RemoteProtocol.Status.PENDING -> return TestResult.Pending
                RemoteProtocol.Status.COMPLETE -> {
                    if (remoteTestResult is RemoteTestResult2) {
                        if (remoteTestResult.result?.negativeResult == false) {
                            return TestResult.NoNegativeTestResult
                        }
                    }

                    if (!remoteTestResult.hasEvents()) {
                        return TestResult.NoNegativeTestResult
                    }

                    return TestResult.NegativeTestResult(
                        remoteTestResult,
                        signedResponseWithTestResult
                    )
                }
                else -> throw IllegalStateException("Unsupported status ${remoteTestResult.status}")
            }
        } catch (e: Exception) {
            return TestResult.Error(
                errorResult = AppErrorResult(
                    step = HolderStep.TestResultNetworkRequest,
                    e = e
                )
            )
        }
    }
}

sealed class TestResult {
    object NoNegativeTestResult : TestResult()
    data class NegativeTestResult(
        val remoteTestResult: RemoteProtocol,
        val signedResponseWithTestResult: SignedResponseWithModel<RemoteProtocol>
    ) : TestResult()

    object Pending : TestResult()
    object EmptyToken : TestResult()
    object InvalidToken : TestResult()
    object UnknownTestProvider : TestResult()
    object EmptyVerificationCode : TestResult()
    object VerificationRequired : TestResult()
    data class Error(val errorResult: ErrorResult) : TestResult()
}
