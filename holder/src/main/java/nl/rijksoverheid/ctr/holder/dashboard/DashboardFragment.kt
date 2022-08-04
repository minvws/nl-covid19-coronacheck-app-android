/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import nl.rijksoverheid.ctr.appconfig.AppConfigViewModel
import nl.rijksoverheid.ctr.appconfig.usecases.ClockDeviationUseCase
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardItem
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardSync
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardTabItem
import nl.rijksoverheid.ctr.holder.dashboard.util.MenuUtil
import nl.rijksoverheid.ctr.holder.databinding.FragmentDashboardBinding
import nl.rijksoverheid.ctr.persistence.PersistenceManager
import nl.rijksoverheid.ctr.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val dashboardViewModel: DashboardViewModel by viewModel()
    private val args: DashboardFragmentArgs by navArgs()
    private val dialogUtil: DialogUtil by inject()
    private val persistenceManager: PersistenceManager by inject()
    private val clockDeviationUseCase: ClockDeviationUseCase by inject()
    private val appConfigViewModel: AppConfigViewModel by sharedViewModel()
    private val menuUtil: MenuUtil by inject()

    /** count of amount of tabs visible. When tab amount changes on policy change the adapter items need to be reset */
    private var tabItemsCount = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDashboardBinding.bind(view)
        val adapter = DashboardPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle, args.returnUri)

        setupViewPager(adapter)
        observeServerTimeSynced()
        observeItems(adapter)
        observeSyncErrors()
        observeAppConfig()
    }

    private fun setupViewPager(adapter: DashboardPagerAdapter) {
        binding.viewPager.adapter = adapter
    }

    /**
     * Whenever the server time is synced we want to refresh our dashboard to check
     * if we want to inform the user that the clock is not correct
     */
    private fun observeServerTimeSynced() {
        clockDeviationUseCase.serverTimeSyncedLiveData.observe(viewLifecycleOwner, EventObserver {
            dashboardViewModel.refresh(
                dashboardSync = DashboardSync.DisableSync
            )
        })
    }

    private fun observeItems(adapter: DashboardPagerAdapter) {
        dashboardViewModel.dashboardTabItemsLiveData.observe(viewLifecycleOwner) { dashboardTabItems ->

            val init = adapter.itemCount == 0

            setupTabs(
                binding = binding,
                items = dashboardTabItems,
                init = init
            )

            // Setup adapter only once
            if (init || adapter.itemCount != tabItemsCount) {
                tabItemsCount = dashboardTabItems.count()

                adapter.setItems(dashboardTabItems)

                // Default select the item that we had selected last
                binding.viewPager.setCurrentItem(
                    persistenceManager.getSelectedDashboardTab(),
                    false
                )

                // Register listener so that last selected item is saved
                binding.viewPager.registerOnPageChangeCallback(object :
                    ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        persistenceManager.setSelectedDashboardTab(position)
                    }
                })
            }

            // This button needs to be shown in this view instead of MyOverviewFragment (which is a single item in the viewpager)
            binding.addQrButton.isVisible = dashboardTabItems.any { dashboardTabItem ->
                dashboardTabItem.items.any { it is DashboardItem.AddQrButtonItem }
            }
            binding.addQrButton.setOnClickListener {
                navigateSafety(
                    DashboardFragmentDirections.actionChooseProofType()
                )
            }
        }
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
                                    dashboardSync = DashboardSync.ForceSync
                                )
                            },
                            negativeButtonText = R.string.dialog_close
                        )
                    } else if (it !is DatabaseSyncerResult.Failed.ServerError) {
                        dialogUtil.presentDialog(
                            context = requireContext(),
                            title = R.string.dialog_title_no_internet,
                            message = getString(R.string.dialog_update_credentials_no_internet),
                            positiveButtonText = R.string.app_status_internet_required_action,
                            positiveButtonCallback = {
                                refresh(
                                    dashboardSync = DashboardSync.ForceSync
                                )
                            },
                            negativeButtonText = R.string.dialog_close
                        )
                    }
                }
            }
        )
    }

    private fun observeAppConfig() {
        appConfigViewModel.appStatusLiveData.observe(viewLifecycleOwner) {
            dashboardViewModel.refresh()
        }
    }

    private fun refresh(dashboardSync: DashboardSync = DashboardSync.CheckSync) {
        dashboardViewModel.refresh(dashboardSync)
    }

    private fun setupTabs(
        binding: FragmentDashboardBinding,
        items: List<DashboardTabItem>,
        init: Boolean
    ) {
        if (items.size == 1) {
            binding.tabs.visibility = View.GONE
            binding.tabsSeparator.visibility = View.GONE
        } else {
            binding.tabs.visibility = View.VISIBLE
            binding.tabsSeparator.visibility = View.VISIBLE

            if (init) {
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
        }
    }

    override fun onPause() {
        super.onPause()

        // Do this check because our screenshot fragment tests run in it's own test activity
        if (parentFragment != null && requireParentFragment().parentFragment != null) {
            (requireParentFragment().requireParentFragment() as HolderMainFragment).getToolbar().menu.clear()
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()

        getToolbar().let { toolbar ->
            if (toolbar?.menu?.size() == 0) {
                toolbar.apply {
                    inflateMenu(R.menu.menu_toolbar)
                    menu.findItem(R.id.action_menu).actionView?.setOnClickListener {
                        menuUtil.showMenu(this@DashboardFragment)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getToolbar() = (parentFragment?.parentFragment as HolderMainFragment?)?.getToolbar()
}
