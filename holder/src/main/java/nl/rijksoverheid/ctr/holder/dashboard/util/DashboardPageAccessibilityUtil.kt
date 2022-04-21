package nl.rijksoverheid.ctr.holder.dashboard.util

import com.xwray.groupie.Group
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.dashboard.items.DashboardInfoCardAdapterItem

/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */
interface DashboardPageAccessibilityUtil {
    fun announceNewInfoItems(oldAdapterItems: List<Group>, newAdapterItems: List<BindableItem<*>>)
}

class DashboardPageAccessibilityUtilImpl : DashboardPageAccessibilityUtil {
    override fun announceNewInfoItems(
        oldAdapterItems: List<Group>,
        newAdapterItems: List<BindableItem<*>>
    ) {
        val oldInfoItems = oldAdapterItems.filterIsInstance<DashboardInfoCardAdapterItem>()
        val newInfoItems = newAdapterItems.filterIsInstance<DashboardInfoCardAdapterItem>()

        newInfoItems.find { !oldInfoItems.map { it.infoItem.javaClass }.contains(it.infoItem.javaClass) }?.focusForAccessibility()
    }
}