package nl.rijksoverheid.ctr.verifier.usecases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.appconfig.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.shared.util.QrCodeUtil
import nl.rijksoverheid.ctr.shared.util.TestResultUtil
import nl.rijksoverheid.ctr.verifier.models.DecryptedQr
import java.util.concurrent.TimeUnit

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class TestResultValidUseCase(
    private val decryptHolderQrUseCase: DecryptHolderQrUseCase,
    private val testResultUtil: TestResultUtil,
    private val qrCodeUtil: QrCodeUtil,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase
) {

    suspend fun valid(qrContent: String): TestResultValidResult = withContext(Dispatchers.IO) {
        when (val decryptResult = decryptHolderQrUseCase.decrypt(qrContent)) {
            is DecryptHolderQrUseCase.DecryptResult.Success -> {
                val validity =
                    TimeUnit.HOURS.toSeconds(
                        cachedAppConfigUseCase.getCachedAppConfigMaxValidityHours().toLong()
                    )
                val isValid = testResultUtil.isValid(
                    sampleDate = decryptResult.decryptQr.sampleDate,
                    validitySeconds = validity
                ) && qrCodeUtil.isValid(
                    creationDate = decryptResult.decryptQr.creationDate
                )
                if (isValid) {
                    TestResultValidResult.Valid(decryptResult.decryptQr)
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
        class Valid(val decryptedQr: DecryptedQr) : TestResultValidResult()
        object Invalid : TestResultValidResult()
    }
}
