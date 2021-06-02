package nl.rijksoverheid.ctr.verifier.ui.scanner.usecases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.verifier.ui.scanner.models.VerifiedQrResultState

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface TestResultValidUseCase {
    suspend fun validate(qrContent: String): VerifiedQrResultState
}

class TestResultValidUseCaseImpl(
    private val verifyQrUseCase: VerifyQrUseCase,
) : TestResultValidUseCase {

    override suspend fun validate(qrContent: String): VerifiedQrResultState =
        withContext(Dispatchers.IO) {
            when (val verifyQrResult = verifyQrUseCase.get(qrContent)) {
                is VerifyQrUseCase.VerifyQrResult.Success -> {
                    val verifiedQr = verifyQrResult.verifiedQr
                    val europeanQrCodeInNL = verifiedQr.testResultAttributes.isNLDCC == "1"
                    when {
                        verifiedQr.testResultAttributes.isSpecimen == "1" -> VerifiedQrResultState.Demo(verifiedQr)
                        europeanQrCodeInNL -> VerifiedQrResultState.Invalid(verifiedQr)
                        else -> VerifiedQrResultState.Valid(verifiedQr)
                    }
                }
                is VerifyQrUseCase.VerifyQrResult.Failed -> {
                    VerifiedQrResultState.Error(verifyQrResult.error)
                }
            }
        }
}
