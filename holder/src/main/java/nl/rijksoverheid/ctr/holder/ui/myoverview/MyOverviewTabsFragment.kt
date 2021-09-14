package nl.rijksoverheid.ctr.holder.ui.myoverview

import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentTabsMyOverviewBinding
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.DashboardTabItem
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class MyOverviewTabsFragment : Fragment(R.layout.fragment_tabs_my_overview) {

    private val dashboardViewModel: DashboardViewModel by viewModel()
    private val viewModel: MyOverviewTabsViewModel by viewModel()
    private val args: MyOverviewTabsFragmentArgs by navArgs()
    private val dialogUtil: DialogUtil by inject()
    private val persistenceManager: PersistenceManager by inject()

    private val refreshHandler = Handler(Looper.getMainLooper())
    private val refreshRunnable = Runnable {
        refresh()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentTabsMyOverviewBinding.bind(view)
        val adapter = DashboardPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle, args.returnUri)

        handleAddQrButton(binding)
        setupViewPager(binding, adapter)
        observeItems(binding, adapter)
        observeSyncErrors()
    }

    private fun handleAddQrButton(binding: FragmentTabsMyOverviewBinding) {
        binding.addQrButton.setOnClickListener {
            navigateSafety(
                MyOverviewFragmentDirections.actionQrType()
            )
        }

        viewModel.showAddCertificateButtonEvent.observe(viewLifecycleOwner) {
            binding.addQrButton.visibility = if (it) VISIBLE else GONE
        }
    }

    private fun setupViewPager(
        binding: FragmentTabsMyOverviewBinding,
        adapter: DashboardPagerAdapter) {
        binding.viewPager.adapter = adapter
    }

    private fun observeItems(
        binding: FragmentTabsMyOverviewBinding,
        adapter: DashboardPagerAdapter) {
        dashboardViewModel.dashboardTabItems.observe(viewLifecycleOwner, {

            // Add pager items only once
            if (adapter.itemCount == 0) {
                adapter.setItems(it)

                setupTabs(
                    binding = binding,
                    items = it
                )

                // Default select the item that we had selected last
                binding.viewPager.setCurrentItem(persistenceManager.getSelectedDashboardTab(), false)

                // Register listener so that last selected item is saved
                binding.viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        persistenceManager.setSelectedDashboardTab(position)
                    }
                })
            }
        })
    }

    private fun observeSyncErrors() {
        dashboardViewModel.databaseSyncerResultLiveData.observe(viewLifecycleOwner,
            EventObserver {
                if (it is DatabaseSyncerResult.Failed) {
                    if (it is DatabaseSyncerResult.Failed.NetworkError && it.hasGreenCardsWithoutCredentials) {
                        dialogUtil.presentDialog(
                            context = requireContext(),
                            title = R.string.dialog_title_no_internet,
                            message = getString(R.string.dialog_credentials_expired_no_internet),
                            positiveButtonText = R.string.app_status_internet_required_action,
                            positiveButtonCallback = {
                                refresh(
                                    forceSync = true
                                )
                            },
                            negativeButtonText = R.string.dialog_close,
                        )
                    } else {
                        dialogUtil.presentDialog(
                            context = requireContext(),
                            title = R.string.dialog_title_no_internet,
                            message = getString(R.string.dialog_update_credentials_no_internet),
                            positiveButtonText = R.string.app_status_internet_required_action,
                            positiveButtonCallback = {
                                refresh(
                                    forceSync = true
                                )
                            },
                            negativeButtonText = R.string.dialog_close,
                        )
                    }
                }
            }
        )
    }

    private fun refresh(forceSync: Boolean = false) {
        dashboardViewModel.refresh(forceSync)
        refreshHandler.postDelayed(
            refreshRunnable,
            TimeUnit.SECONDS.toMillis(10)
        )
    }

    private fun setupTabs(binding: FragmentTabsMyOverviewBinding,
                          items: List<DashboardTabItem>) {
        TabLayoutMediator(binding.tabs, binding.viewPager) { tab, position ->
            tab.view.setOnLongClickListener {
                true
            }
            tab.text = getString(items[position].title)
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

            override fun onTabReselected(tab: TabLayout.Tab) {
                val textView = tab.view.children.find { it is TextView } as? TextView
                textView?.setTypeface(null, Typeface.BOLD)
            }
        })

        // Call selectTab so that styling get's picked up on launch
        binding.tabs.selectTab(binding.tabs.getTabAt(0))
    }

    override fun onPause() {
        super.onPause()
        refreshHandler.removeCallbacks(refreshRunnable)
        (parentFragment?.parentFragment as HolderMainFragment).getToolbar().menu.clear()
    }

    override fun onResume() {
        super.onResume()
        refresh()

        (parentFragment?.parentFragment as HolderMainFragment?)?.getToolbar().let { toolbar ->
            if (toolbar?.menu?.size() == 0) {
                toolbar.apply {
                    inflateMenu(R.menu.overview_toolbar)
                }
            }
        }
    }
}
