package nl.rijksoverheid.ctr.verifier.usecases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.api.repositories.TestResultRepository
import nl.rijksoverheid.ctr.shared.util.QrCodeUtil
import nl.rijksoverheid.ctr.shared.util.TestResultUtil

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class TestResultValidUseCase(
    private val decryptHolderQrUseCase: DecryptHolderQrUseCase,
    private val testResultRepository: nl.rijksoverheid.ctr.api.repositories.TestResultRepository,
    private val testResultUtil: TestResultUtil,
    private val qrCodeUtil: QrCodeUtil
) {

    suspend fun valid(qrContent: String): TestResultValidResult = withContext(Dispatchers.IO) {
        when (val decryptResult = decryptHolderQrUseCase.decrypt(qrContent)) {
            is DecryptHolderQrUseCase.DecryptResult.Success -> {
                val validity = testResultRepository.getTestValiditySeconds()
                val isValid = testResultUtil.isValid(
                    sampleDate = decryptResult.decryptQr.sampleDate,
                    validitySeconds = validity
                ) && qrCodeUtil.isValid(
                    creationDate = decryptResult.decryptQr.creationDate
                )
                if (isValid) {
                    TestResultValidResult.Valid
                } else {
                    TestResultValidResult.Invalid
                }
            }
            is DecryptHolderQrUseCase.DecryptResult.Failed -> {
                TestResultValidResult.Invalid
            }
        }
    }

    sealed class TestResultValidResult {
        object Valid : TestResultValidResult()
        object Invalid : TestResultValidResult()
    }
}
