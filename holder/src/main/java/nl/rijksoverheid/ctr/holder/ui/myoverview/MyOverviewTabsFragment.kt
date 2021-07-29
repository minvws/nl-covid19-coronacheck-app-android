package nl.rijksoverheid.ctr.holder.ui.myoverview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentTabsMyOverviewBinding
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.shared.ext.sharedViewModelWithOwner
import org.koin.androidx.viewmodel.ViewModelOwner
import org.koin.android.ext.android.inject

class TabPagesAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        // Return a NEW fragment instance in createFragment(int)
        println("GIO create new MyOverviewFragment")
        val fragment = MyOverviewFragment()
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

    companion object {
        private const val domesticPosition = 0
        private const val euPosition = 1
    }

    private val persistenceManager: PersistenceManager by inject()

    private val tabsMap = mapOf(GreenCardType.Domestic to domesticPosition, GreenCardType.Eu to euPosition)

    private val myOverviewViewModel: MyOverviewViewModel by sharedViewModelWithOwner(
        owner = {
            ViewModelOwner.from(
                findNavController().getViewModelStoreOwner(R.id.nav_graph_overview),
                this
            )
        })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentTabsMyOverviewBinding.inflate(inflater, container, false)
        val view = binding.root

        val viewPagerAdapter = TabPagesAdapter(this)

        binding.viewPager.adapter = viewPagerAdapter

        TabLayoutMediator(binding.tabs, binding.viewPager) { tab, position ->
            tab.text = arrayOf(getString(R.string.travel_button_domestic), getString(R.string.travel_button_europe))[position]
            tab.id = position
        }.attach()

        binding.tabs.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.id) {
                    domesticPosition -> {
                        myOverviewViewModel.refreshOverviewItems(GreenCardType.Domestic)
                    }
                    euPosition -> {
                        myOverviewViewModel.refreshOverviewItems(GreenCardType.Eu)
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
            override fun onTabReselected(tab: TabLayout.Tab?) = Unit
        })

        val defaultTab = binding.tabs.getTabAt(tabsMap[persistenceManager.getSelectedGreenCardType()]!!)
        defaultTab?.select()

        return view
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