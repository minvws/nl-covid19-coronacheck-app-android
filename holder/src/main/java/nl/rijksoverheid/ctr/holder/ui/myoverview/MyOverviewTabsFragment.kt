package nl.rijksoverheid.ctr.holder.ui.myoverview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentTabsMyOverviewBinding
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.GetMyOverviewItemsUseCase
import nl.rijksoverheid.ctr.holder.ui.myoverview.MyOverviewFragment.Companion.GREEN_CARD_TYPE
import nl.rijksoverheid.ctr.holder.ui.myoverview.MyOverviewTabsFragment.Companion.positionTabsMap
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class TabPagesAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        val fragment = MyOverviewFragment()
        fragment.arguments = Bundle().apply {
            putParcelable(GREEN_CARD_TYPE, positionTabsMap[position])
        }
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
        val tabsMap = mapOf(GreenCardType.Domestic to domesticPosition, GreenCardType.Eu to euPosition)
        val positionTabsMap = mapOf(domesticPosition to GreenCardType.Domestic, euPosition to GreenCardType.Eu)
    }

    private val persistenceManager: PersistenceManager by inject()

    private val viewModel: MyOverviewTabsViewModel by viewModel()

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
            tab.view.setOnLongClickListener {
                true
            }
            tab.text = arrayOf(getString(R.string.travel_button_domestic), getString(R.string.travel_button_europe))[position]
        }.attach()

        val defaultTab = binding.tabs.getTabAt(tabsMap[persistenceManager.getSelectedGreenCardType()]!!)
        defaultTab?.select()

        binding.addQrButton.setOnClickListener {
            navigateSafety(
                MyOverviewFragmentDirections.actionQrType()
            )
        }

        viewModel.showAddCertificateButtonEvent.observe(viewLifecycleOwner, EventObserver {
            binding.addQrButton.visibility = if (it) VISIBLE else GONE
        })

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