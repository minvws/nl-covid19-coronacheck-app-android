package nl.rijksoverheid.ctr.verifier.ui.scanlog.usecase

import nl.rijksoverheid.ctr.shared.utils.AndroidUtil
import nl.rijksoverheid.ctr.verifier.persistance.usecase.VerifierCachedAppConfigUseCase
import nl.rijksoverheid.ctr.verifier.ui.scanlog.items.ScanLogItem
import nl.rijksoverheid.ctr.verifier.ui.scanlog.repositories.ScanLogRepository
import java.util.concurrent.TimeUnit

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface GetScanLogItemsUseCase {
    suspend fun getItems(): List<ScanLogItem>
}

class GetScanLogItemsUseCaseImpl(
    private val androidUtil: AndroidUtil,
    private val scanLogRepository: ScanLogRepository,
    private val verifierCachedAppConfigUseCase: VerifierCachedAppConfigUseCase,
): GetScanLogItemsUseCase {
    override suspend fun getItems(): List<ScanLogItem> {
        val scanLogs = scanLogRepository.getAll()
        val firstInstallTime = androidUtil.getFirstInstallTime()
        val scanLogStorageMinutes = TimeUnit.SECONDS.toMinutes(verifierCachedAppConfigUseCase.getCachedAppConfig().scanLogStorageSeconds.toLong())
        val scanLogItems = mutableListOf<ScanLogItem>()

        scanLogItems.add(ScanLogItem.HeaderItem(scanLogStorageMinutes))
        scanLogItems.add(ScanLogItem.ListHeaderItem(scanLogStorageMinutes))

        if (scanLogs.isEmpty()) {
            scanLogItems.add(ScanLogItem.ListEmptyItem)
        } else {
            scanLogs.forEachIndexed { index, scanLog ->
                scanLogItems.add(ScanLogItem.ListScanLogItem(scanLog, index))
            }
        }

        scanLogItems.add(
            ScanLogItem.FirstInstallTimeItem(firstInstallTime)
        )

        return scanLogItems
    }
}