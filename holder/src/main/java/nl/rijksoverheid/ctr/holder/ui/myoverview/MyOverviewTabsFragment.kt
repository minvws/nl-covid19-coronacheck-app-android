package nl.rijksoverheid.ctr.holder.ui.myoverview

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentTabsMyOverviewBinding
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.ui.myoverview.MyOverviewFragment.Companion.GREEN_CARD_TYPE
import nl.rijksoverheid.ctr.holder.ui.myoverview.MyOverviewFragment.Companion.RETURN_URI
import nl.rijksoverheid.ctr.holder.ui.myoverview.MyOverviewTabsFragment.Companion.positionTabsMap
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * viewpager adapter to house green card overviews for domestic and European.
 *
 * @param[fragment] Tabs fragment with viewpager where the overviews are nested within.
 * @param[returnToExternalAppUri] Uri used to return to external app from which it was deep linked from.
 */
class TabPagesAdapter(fragment: Fragment, private val returnToExternalAppUri: String?) :
    FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        val fragment = MyOverviewFragment()
        fragment.arguments = Bundle().apply {
            putParcelable(GREEN_CARD_TYPE, positionTabsMap[position])
            putString(RETURN_URI, returnToExternalAppUri)
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
class MyOverviewTabsFragment : Fragment(R.layout.fragment_tabs_my_overview) {

    companion object {
        private const val domesticPosition = 0
        private const val euPosition = 1
        val tabsMap =
            mapOf(GreenCardType.Domestic to domesticPosition, GreenCardType.Eu to euPosition)
        val positionTabsMap =
            mapOf(domesticPosition to GreenCardType.Domestic, euPosition to GreenCardType.Eu)
    }

    private val persistenceManager: PersistenceManager by inject()

    private val viewModel: MyOverviewTabsViewModel by viewModel()

    private val args: MyOverviewTabsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentTabsMyOverviewBinding.inflate(inflater, container, false)
        val view = binding.root

        val viewPagerAdapter = TabPagesAdapter(this, args.returnUri)

        binding.viewPager.adapter = viewPagerAdapter

        setupTabs(binding)

        binding.addQrButton.setOnClickListener {
            navigateSafety(
                MyOverviewFragmentDirections.actionQrType()
            )
        }

        viewModel.showAddCertificateButtonEvent.observe(viewLifecycleOwner) {
            binding.addQrButton.visibility = if (it) VISIBLE else GONE
        }

        return view
    }

    private fun setupTabs(binding: FragmentTabsMyOverviewBinding) {
        TabLayoutMediator(binding.tabs, binding.viewPager) { tab, position ->
            tab.view.setOnLongClickListener {
                true
            }
            tab.text = arrayOf(
                getString(R.string.travel_button_domestic),
                getString(R.string.travel_button_europe)
            )[position]
        }.attach()

        binding.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val textView = tab.view.children.find { it is TextView } as? TextView
                textView?.setTypeface(null, Typeface.BOLD)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val textView = tab.view.children.find { it is TextView } as? TextView
                textView?.setTypeface(null, Typeface.NORMAL)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        val defaultTab =
            binding.tabs.getTabAt(tabsMap[getSelectedGreenCardType()]!!)
        defaultTab?.select()
        (defaultTab?.view?.children?.find { it is TextView } as? TextView)?.setTypeface(null, Typeface.BOLD)
    }

    private fun getSelectedGreenCardType() = if (args.returnUri != null) {
        GreenCardType.Domestic
    } else {
        persistenceManager.getSelectedGreenCardType()
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
