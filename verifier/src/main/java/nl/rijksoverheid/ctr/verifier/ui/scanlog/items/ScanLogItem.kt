package nl.rijksoverheid.ctr.verifier.ui.scanlog.items

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
sealed class ScanLogItem {
    data class HeaderItem(val scanLogStorageMinutes: Long): ScanLogItem()
    data class ListHeaderItem(val scanLogStorageMinutes: Long): ScanLogItem()
}