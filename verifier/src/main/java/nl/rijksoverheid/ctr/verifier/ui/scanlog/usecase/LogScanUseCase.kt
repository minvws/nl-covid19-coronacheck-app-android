package nl.rijksoverheid.ctr.verifier.ui.scanlog.usecase

import nl.rijksoverheid.ctr.verifier.persistance.database.VerifierDatabase
import nl.rijksoverheid.ctr.verifier.persistance.database.entities.ScanLogEntity
import nl.rijksoverheid.ctr.verifier.ui.policy.VerificationPolicySelectionUseCase
import java.time.Clock
import java.time.Instant

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface LogScanUseCase {
    suspend fun log()
}

class LogScanUseCaseImpl(
    private val clock: Clock,
    private val verificationPolicySelectionUseCase: VerificationPolicySelectionUseCase,
    private val verifierDatabase: VerifierDatabase
): LogScanUseCase {

    override suspend fun log() {
        verifierDatabase.scanLogDao().insert(
            ScanLogEntity(
                policy = verificationPolicySelectionUseCase.get(),
                date = Instant.now(clock)
            )
        )
    }
}