package nl.rijksoverheid.ctr.verifier.ui.scanner.usecases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import nl.rijksoverheid.ctr.shared.models.VerificationResult

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface VerifyQrUseCase {
    suspend fun get(
        content: String
    ): VerifyQrResult

    sealed class VerifyQrResult {
        data class Success(val verifiedQr: VerificationResult) : VerifyQrResult()
        data class Failed(val error: String) : VerifyQrResult()
    }
}

class VerifyQrUseCaseImpl(
    private val mobileCoreWrapper: MobileCoreWrapper
) : VerifyQrUseCase {

    override suspend fun get(
        content: String
    ): VerifyQrUseCase.VerifyQrResult = withContext(Dispatchers.IO) {
        try {
            VerifyQrUseCase.VerifyQrResult.Success(
                mobileCoreWrapper.verify(content.toByteArray())
            )
        } catch (e: Exception) {
            VerifyQrUseCase.VerifyQrResult.Failed(e.toString())
        }
    }
}
