package nl.rijksoverheid.ctr.verifier.usecases

import clmobile.Clmobile
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.shared.ext.toObject
import nl.rijksoverheid.ctr.shared.ext.verify
import nl.rijksoverheid.ctr.verifier.models.VerifiedQr

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
        class Success(val verifiedQr: VerifiedQr) : VerifyQrResult()
        object Failed : VerifyQrResult()
    }
}

class VerifyQrUseCaseImpl(
    private val moshi: Moshi
) : VerifyQrUseCase {

    override suspend fun get(
        content: String
    ): VerifyQrUseCase.VerifyQrResult = withContext(Dispatchers.IO) {
        try {
            val result =
                Clmobile.verifyQREncoded(
                    content.toByteArray()
                ).verify()

            VerifyQrUseCase.VerifyQrResult.Success(
                VerifiedQr(
                    creationDateSeconds = result.unixTimeSeconds,
                    testResultAttributes = result.attributesJson.decodeToString().toObject(
                        moshi
                    )
                )
            )
        } catch (e: Exception) {
            VerifyQrUseCase.VerifyQrResult.Failed
        }
    }
}
