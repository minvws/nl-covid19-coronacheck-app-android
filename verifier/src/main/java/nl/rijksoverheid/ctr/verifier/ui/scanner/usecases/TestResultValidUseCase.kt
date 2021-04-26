package nl.rijksoverheid.ctr.verifier.ui.scanner.usecases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.appconfig.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.shared.util.TestResultUtil
import nl.rijksoverheid.ctr.verifier.ui.scanner.models.VerifiedQrResultState
import nl.rijksoverheid.ctr.verifier.ui.scanner.utils.QrCodeUtil
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
interface TestResultValidUseCase {
    suspend fun validate(qrContent: String): VerifiedQrResultState
}

class TestResultValidUseCaseImpl(
    private val verifyQrUseCase: VerifyQrUseCase,
    private val testResultUtil: TestResultUtil,
    private val qrCodeUtil: QrCodeUtil,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase
) : TestResultValidUseCase {

    override suspend fun validate(qrContent: String): VerifiedQrResultState =
        withContext(Dispatchers.IO) {
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
                        if (verifiedQr.testResultAttributes.isSpecimen == "1") {
                            VerifiedQrResultState.Demo(verifiedQr)
                        } else {
                            VerifiedQrResultState.Valid(verifiedQr)
                        }
                    } else {
                        VerifiedQrResultState.Invalid(verifiedQr)
                    }
                }
                is VerifyQrUseCase.VerifyQrResult.Failed -> {
                    VerifiedQrResultState.Error(verifyQrResult.error)
                }
            }
        }
}
