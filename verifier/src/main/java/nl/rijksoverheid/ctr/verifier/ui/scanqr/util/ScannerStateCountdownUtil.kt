package nl.rijksoverheid.ctr.verifier.ui.scanqr.util

import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
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
interface ScannerStateCountdownUtil {
    fun getRemainingSecondsLocked(): Long
}

class ScannerStateCountdownUtilImpl(
    private val persistenceManager: PersistenceManager,
    private val clock: Clock,
    private val cachedAppConfigUseCase: VerifierCachedAppConfigUseCase,
): ScannerStateCountdownUtil {
    override fun getRemainingSecondsLocked(): Long {
        val now = OffsetDateTime.now(clock).toEpochSecond()
        val lockSeconds = cachedAppConfigUseCase.getCachedAppConfig().scanLockSeconds.toLong()

        val lastScanLockTimeSeconds = persistenceManager.getLastScanLockTimeSeconds()

        return lockSeconds + lastScanLockTimeSeconds - now
    }
}
