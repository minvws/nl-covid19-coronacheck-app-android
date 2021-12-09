package nl.rijksoverheid.ctr.verifier.ui.policy

import nl.rijksoverheid.ctr.verifier.persistance.usecase.VerifierCachedAppConfigUseCase
import nl.rijksoverheid.ctr.verifier.ui.scanlog.repositories.ScanLogRepository
import java.time.Clock
import java.time.OffsetDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface GetRecentScanLogsUseCase {
    suspend fun get(): Boolean
}

class GetRecentScanLogsUseCaseImpl(
    private val scanLogRepository: ScanLogRepository,
    private val clock: Clock,
    private val cachedAppConfigUseCase: VerifierCachedAppConfigUseCase,
): GetRecentScanLogsUseCase {
    override suspend fun get(): Boolean {
        return scanLogRepository.getAll().any { it.from.isAfter(OffsetDateTime.now(clock).minusSeconds(cachedAppConfigUseCase.getCachedAppConfig().scanLogStorageSeconds.toLong())) }
    }
}