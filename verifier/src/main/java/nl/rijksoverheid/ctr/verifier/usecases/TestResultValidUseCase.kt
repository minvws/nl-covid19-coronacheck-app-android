package nl.rijksoverheid.ctr.verifier.usecases

import nl.rijksoverheid.ctr.shared.repositories.TestResultRepository
import nl.rijksoverheid.ctr.shared.util.TestResultUtil
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

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
    private val testResultUtil: TestResultUtil
) {

    suspend fun valid(currentDate: OffsetDateTime, qrContent: String): Boolean {
        val sampleDateSeconds = decryptHolderQrUseCase.decrypt(qrContent)
        val validity = testResultRepository.getTestValidity()
        return testResultUtil.isValid(
            currentDate = currentDate,
            sampleDate = OffsetDateTime.ofInstant(
                Instant.ofEpochSecond(sampleDateSeconds),
                ZoneOffset.UTC
            ),
            validitySeconds = validity
        )
    }
}
