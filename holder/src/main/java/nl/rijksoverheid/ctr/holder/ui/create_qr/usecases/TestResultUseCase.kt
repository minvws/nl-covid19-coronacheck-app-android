package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.HolderStep
import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteProtocol
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteTestResult2
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.SignedResponseWithModel
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.CoronaCheckRepository
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.TestProviderRepository
import nl.rijksoverheid.ctr.holder.ui.myoverview.usecases.TestResultAttributesUseCase
import nl.rijksoverheid.ctr.holder.ui.myoverview.utils.TokenValidatorUtil
import nl.rijksoverheid.ctr.holder.ui.myoverview.utils.TokenValidatorUtilImpl
import nl.rijksoverheid.ctr.shared.ext.removeWhitespace
import nl.rijksoverheid.ctr.shared.models.AppErrorResult
import nl.rijksoverheid.ctr.shared.models.ErrorResult
import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult
import nl.rijksoverheid.ctr.shared.utils.PersonalDetailsUtil
import nl.rijksoverheid.ctr.shared.utils.TestResultUtil
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
    private val configProviderUseCase: ConfigProvidersUseCase,
    private val testProviderRepository: TestProviderRepository,
    private val coronaCheckRepository: CoronaCheckRepository,
    private val commitmentMessageUseCase: CommitmentMessageUseCase,
    private val secretKeyUseCase: SecretKeyUseCase,
    private val createCredentialUseCase: CreateCredentialUseCase,
    private val personalDetailsUtil: PersonalDetailsUtil,
    private val testResultUtil: TestResultUtil,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase,
    private val testResultAttributesUseCase: TestResultAttributesUseCase,
    private val tokenValidatorUtil: TokenValidatorUtil
) {

    suspend fun testResult(uniqueCode: String, verificationCode: String? = null): TestResult {
        try {
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

            // Disable the luhn check for now since providers do not yet support it
            // We need to check for valid chars
            token.toCharArray().forEach {
                if (!TokenValidatorUtilImpl.CODE_POINTS.contains(it)) {
                    return TestResult.InvalidToken
                }
            }

//        if (!tokenValidatorUtil.validate(
//                token = token,
//                checksum = checksum
//            )
//        ) {
//            return TestResult.InvalidToken
//        }

            val testProvider = configProviderUseCase.testProvider(providerIdentifier)
                ?: return TestResult.InvalidToken

            val signedResponseWithTestResultRequestResult = testProviderRepository.remoteTestResult(
                url = testProvider.resultUrl,
                token = token.removeWhitespace(),
                verifierCode = verificationCode?.removeWhitespace(),
                signingCertificateBytes = testProvider.publicKey
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
                    step = HolderStep.TestProvidersNetworkRequest,
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
    object InvalidToken : TestResult()
    object VerificationRequired : TestResult()
    data class Error(val errorResult: ErrorResult) : TestResult()
}
