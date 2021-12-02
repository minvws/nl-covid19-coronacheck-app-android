package nl.rijksoverheid.ctr.verifier.ui.scanlog.usecase

import nl.rijksoverheid.ctr.verifier.persistance.usecase.VerifierCachedAppConfigUseCase
import nl.rijksoverheid.ctr.verifier.ui.scanlog.items.ScanLogItem
import java.util.concurrent.TimeUnit

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface GetScanLogItemsUseCase {
    fun getItems(): List<ScanLogItem>
}

class GetScanLogItemsUseCaseImpl(
    private val verifierCachedAppConfigUseCase: VerifierCachedAppConfigUseCase,
): GetScanLogItemsUseCase {
    override fun getItems(): List<ScanLogItem> {
        val scanLogStorageMinutes = TimeUnit.SECONDS.toMinutes(verifierCachedAppConfigUseCase.getCachedAppConfig().scanLogStorageSeconds.toLong())
        val headerItem = ScanLogItem.HeaderItem(scanLogStorageMinutes)
        return listOf(headerItem)
    }
}