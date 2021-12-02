package nl.rijksoverheid.ctr.verifier.ui.scanlog.usecase

import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import nl.rijksoverheid.ctr.verifier.persistance.database.entities.ScanLogEntity
import nl.rijksoverheid.ctr.verifier.persistance.usecase.VerifierCachedAppConfigUseCase
import nl.rijksoverheid.ctr.verifier.ui.scanlog.items.ScanLogItem
import nl.rijksoverheid.ctr.verifier.ui.scanlog.models.ScanLog
import java.time.OffsetDateTime
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
        val listHeaderItem = ScanLogItem.ListHeaderItem(scanLogStorageMinutes)
        val dummyListItem = ScanLogItem.ScanLogListItem(ScanLog(
            policy = VerificationPolicy.VerificationPolicy2G,
            countFrom = 0,
            countTo = 10,
            skew = true,
            from = ScanLog.ScanLogDate.Date(OffsetDateTime.now().minusHours(5)),
            to = ScanLog.ScanLogDate.Now
        ))
        return listOf(headerItem, listHeaderItem, dummyListItem)
    }
}