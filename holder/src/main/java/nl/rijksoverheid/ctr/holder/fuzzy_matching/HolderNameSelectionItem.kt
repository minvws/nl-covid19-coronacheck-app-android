package nl.rijksoverheid.ctr.holder.fuzzy_matching

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
sealed class HolderNameSelectionItem {
    object HeaderItem : HolderNameSelectionItem()

    data class ListItem(
        val name: String,
        val events: String,
        val isSelected: Boolean = false,
        val willBeRemoved: Boolean = false,
        val nothingSelectedError: Boolean = false,
        val detailData: List<SelectionDetailData> = listOf()
    ) : HolderNameSelectionItem()

    object FooterItem : HolderNameSelectionItem()
}
