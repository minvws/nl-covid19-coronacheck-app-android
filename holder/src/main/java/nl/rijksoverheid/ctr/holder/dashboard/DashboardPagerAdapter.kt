/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardTabItem

/**
 * viewpager adapter to house green card overviews for domestic and European.
 *
 * @param[fragment] Tabs fragment with viewpager where the overviews are nested within.
 * @param[returnToExternalAppUri] Uri used to return to external app from which it was deep linked from.
 */
class DashboardPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val returnToExternalAppUri: String?
) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    private val items: List<DashboardTabItem> = mutableListOf()

    fun setItems(items: List<DashboardTabItem>) {
        (this.items as MutableList<DashboardTabItem>).clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun createFragment(position: Int): Fragment {
        return DashboardPageFragment.getInstance(
            greenCardType = items[position].greenCardType,
            returnUri = returnToExternalAppUri
        )
    }

    override fun getItemId(position: Int): Long {
        return items[position].title.toLong()
    }

    override fun containsItem(itemId: Long): Boolean = items.any { it.title.toLong() == itemId }
}
