package nl.rijksoverheid.ctr.verifier.ui.scanlog.usecase

import nl.rijksoverheid.ctr.verifier.ui.scanlog.items.ScanLogItem

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

class GetScanLogItemsUseCaseImpl: GetScanLogItemsUseCase {
    override fun getItems(): List<ScanLogItem> {
        return listOf(ScanLogItem.HeaderItem)
    }
}