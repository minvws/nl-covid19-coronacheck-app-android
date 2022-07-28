package nl.rijksoverheid.ctr.verifier.scanlog.items

import java.time.OffsetDateTime
import nl.rijksoverheid.ctr.verifier.scanlog.models.ScanLog

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
sealed class ScanLogItem {
    data class HeaderItem(val scanLogStorageMinutes: Long) : ScanLogItem()
    data class ListHeaderItem(val scanLogStorageMinutes: Long) : ScanLogItem()
    data class ListScanLogItem(val scanLog: ScanLog, val index: Int) : ScanLogItem()
    object ListEmptyItem : ScanLogItem()
    data class FirstInstallTimeItem(val firstInstallTime: OffsetDateTime) : ScanLogItem()
}
