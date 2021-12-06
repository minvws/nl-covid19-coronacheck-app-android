package nl.rijksoverheid.ctr.verifier.ui.scanlog.usecase

import nl.rijksoverheid.ctr.verifier.persistance.database.VerifierDatabase
import nl.rijksoverheid.ctr.verifier.persistance.usecase.VerifierCachedAppConfigUseCase
import java.time.Clock
import java.time.OffsetDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface ScanLogsCleanupUseCase {
    suspend fun cleanup()
}

class ScanLogsCleanupUseCaseImpl(
    private val clock: Clock,
    private val verifierDatabase: VerifierDatabase,
    private val verifierCachedAppConfigUseCase: VerifierCachedAppConfigUseCase): ScanLogsCleanupUseCase {
    override suspend fun cleanup() {
        val scanLogs = verifierDatabase.scanLogDao().getAll()
        val scanLogsStorageSeconds = verifierCachedAppConfigUseCase.getCachedAppConfig().scanLogStorageSeconds

        val scanLogsToRemove = scanLogs.filter {
            it.date.plusSeconds(scanLogsStorageSeconds.toLong()).isAfter(OffsetDateTime.now(clock))
        }

        verifierDatabase.scanLogDao().delete(scanLogsToRemove)
    }
}