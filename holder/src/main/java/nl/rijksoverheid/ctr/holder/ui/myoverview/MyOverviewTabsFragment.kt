package nl.rijksoverheid.ctr.holder.ui.myoverview

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentTabsMyOverviewBinding

class TabPagesAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        // Return a NEW fragment instance in createFragment(int)
        val fragment = Fragment()
//        fragment.arguments = Bundle().apply {
//            putInt(ARG_OBJECT, position + 1)
//        }
        return fragment
    }
}

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class MyOverviewTabsFragment: Fragment(R.layout.fragment_tabs_my_overview) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentTabsMyOverviewBinding.bind(view)

        val viewPagerAdapter = TabPagesAdapter(this)

        binding.viewPager.adapter = viewPagerAdapter

        TabLayoutMediator(binding.tabs, binding.viewPager) { tab, position ->
            tab.text = arrayOf(getString(R.string.travel_button_domestic), getString(R.string.travel_button_europe))[position]
        }.attach()
    }

    override fun onPause() {
        super.onPause()

        (parentFragment?.parentFragment as HolderMainFragment).getToolbar().menu.clear()
    }

    override fun onResume() {
        super.onResume()

        (parentFragment?.parentFragment as HolderMainFragment?)?.getToolbar().let { toolbar ->
            if (toolbar?.menu?.size() == 0) {
                toolbar.apply {
                    inflateMenu(R.menu.overview_toolbar)
                }
            }
        }
    }
}