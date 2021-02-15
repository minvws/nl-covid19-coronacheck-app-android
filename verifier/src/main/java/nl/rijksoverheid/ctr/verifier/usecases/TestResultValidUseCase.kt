package nl.rijksoverheid.ctr.verifier.usecases

import nl.rijksoverheid.ctr.shared.repositories.TestResultRepository
import nl.rijksoverheid.ctr.shared.util.QrCodeUtil
import nl.rijksoverheid.ctr.shared.util.TestResultUtil
import java.time.OffsetDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class TestResultValidUseCase(
    private val decryptHolderQrUseCase: DecryptHolderQrUseCase,
    private val testResultRepository: TestResultRepository,
    private val testResultUtil: TestResultUtil,
    private val qrCodeUtil: QrCodeUtil
) {

    suspend fun valid(currentDate: OffsetDateTime, qrContent: String): Boolean {
        val decryptQr = decryptHolderQrUseCase.decrypt(qrContent)
        val validity = testResultRepository.getTestValiditySeconds()
        return testResultUtil.isValid(
            currentDate = currentDate,
            sampleDate = decryptQr.sampleDate,
            validitySeconds = validity
        ) && qrCodeUtil.isValid(
            currentDate = currentDate,
            creationDate = decryptQr.creationDate
        )
    }
}
