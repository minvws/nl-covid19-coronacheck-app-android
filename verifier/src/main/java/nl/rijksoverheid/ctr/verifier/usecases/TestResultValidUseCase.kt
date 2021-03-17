package nl.rijksoverheid.ctr.verifier.usecases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.appconfig.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.shared.util.QrCodeUtil
import nl.rijksoverheid.ctr.shared.util.TestResultUtil
import nl.rijksoverheid.ctr.verifier.models.VerifiedQr
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class TestResultValidUseCase(
    private val verifyQrUseCase: VerifyQrUseCase,
    private val testResultUtil: TestResultUtil,
    private val qrCodeUtil: QrCodeUtil,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase
) {

    suspend fun valid(qrContent: String): TestResultValidResult = withContext(Dispatchers.IO) {
        when (val verifyQrResult = verifyQrUseCase.get(qrContent)) {
            is VerifyQrUseCase.VerifyQrResult.Success -> {
                val verifiedQr = verifyQrResult.verifiedQr
                val validity =
                    TimeUnit.HOURS.toSeconds(
                        cachedAppConfigUseCase.getCachedAppConfigMaxValidityHours().toLong()
                    )
                val isValid = testResultUtil.isValid(
                    sampleDate = OffsetDateTime.ofInstant(
                        Instant.ofEpochSecond(verifiedQr.testResultAttributes.sampleTime),
                        ZoneOffset.UTC
                    ),
                    validitySeconds = validity,
                ) && qrCodeUtil.isValid(
                    creationDate = OffsetDateTime.ofInstant(
                        Instant.ofEpochSecond(verifiedQr.creationDateSeconds),
                        ZoneOffset.UTC
                    ),
                    isPaperProof = verifiedQr.testResultAttributes.isPaperProof
                )
                if (isValid) {
                    TestResultValidResult.Valid(verifiedQr)
                } else {
                    TestResultValidResult.Invalid
                }
            }
            is VerifyQrUseCase.VerifyQrResult.Failed -> {
                TestResultValidResult.Invalid
            }
        }
    }

    sealed class TestResultValidResult {
        class Valid(val verifiedQr: VerifiedQr) : TestResultValidResult()
        object Invalid : TestResultValidResult()
    }
}
