package nl.rijksoverheid.ctr.holder.ui.myoverview

import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import android.widget.Toolbar
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import nl.rijksoverheid.ctr.appconfig.AppConfigViewModel
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigPersistenceManager
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.ClockDeviationUseCase
import nl.rijksoverheid.ctr.design.fragments.menu.MenuFragmentDirections
import nl.rijksoverheid.ctr.design.fragments.menu.MenuSection
import nl.rijksoverheid.ctr.design.menu.about.AboutThisAppData
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.holder.BuildConfig
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentTabsMyOverviewBinding
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.DashboardSync
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.DashboardTabItem
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.concurrent.TimeUnit

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class MyOverviewTabsFragment : Fragment(R.layout.fragment_tabs_my_overview) {

    private var _binding: FragmentTabsMyOverviewBinding? = null
    private val binding get() = _binding!!
    private val dashboardViewModel: DashboardViewModel by viewModel()
    private val args: MyOverviewTabsFragmentArgs by navArgs()
    private val dialogUtil: DialogUtil by inject()
    private val persistenceManager: PersistenceManager by inject()
    private val clockDeviationUseCase: ClockDeviationUseCase by inject()
    private val appConfigViewModel: AppConfigViewModel by sharedViewModel()
    private val cachedAppConfigUseCase: CachedAppConfigUseCase by inject()
    private val appConfigPersistenceManager: AppConfigPersistenceManager by inject()

    private val refreshHandler = Handler(Looper.getMainLooper())
    private val refreshRunnable = Runnable {
        refresh()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTabsMyOverviewBinding.bind(view)
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
        dashboardViewModel.dashboardTabItemsLiveData.observe(viewLifecycleOwner, { dashboardTabItems ->

            // Add pager items only once
            if (adapter.itemCount == 0) {
                adapter.setItems(dashboardTabItems)

                setupTabs(
                    binding = binding,
                    items = dashboardTabItems
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

            // This button needs to be shown in this view instead of MyOverviewFragment (which is a single item in the viewpager)
            val showAddQrButton = dashboardTabItems.first().items.any { it is DashboardItem.AddQrButtonItem && it.show }
            binding.addQrButton.visibility = if (showAddQrButton) VISIBLE else GONE
            if (showAddQrButton) {
                binding.addQrButton.setOnClickListener {
                    navigateSafety(
                        MyOverviewFragmentDirections.actionQrType()
                    )
                }
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
                                    dashboardSync = DashboardSync.ForceSync
                                )
                            },
                            negativeButtonText = R.string.dialog_close,
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
                            negativeButtonText = R.string.dialog_close,
                        )
                    }
                }
            }
        )
    }

    private fun observeAppConfig() {
        appConfigViewModel.appStatusLiveData.observe(viewLifecycleOwner, {
            dashboardViewModel.refresh()
        })
    }

    private fun refresh(dashboardSync: DashboardSync = DashboardSync.CheckSync) {
        dashboardViewModel.refresh(dashboardSync)
        refreshHandler.postDelayed(
            refreshRunnable,
            TimeUnit.SECONDS.toMillis(60)
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

        getToolbar().let { toolbar ->
            if (toolbar?.menu?.size() == 0) {
                toolbar.apply {
                    inflateMenu(R.menu.overview_toolbar)
                    setupMenu()
                }
            }
        }
    }

    private fun setupMenu() {
        getToolbar()?.let {
            it.menu.findItem(R.id.action_menu).actionView?.setOnClickListener {
                val aboutThisAppData = AboutThisAppData(
                    versionName = BuildConfig.VERSION_NAME,
                    versionCode = BuildConfig.VERSION_CODE.toString(),
                    sections = listOf(
                        AboutThisAppData.AboutThisAppSection(
                            header = R.string.about_this_app_read_more,
                            items = mutableListOf(
                                AboutThisAppData.Url(
                                    text = getString(R.string.privacy_statement),
                                    url = getString(R.string.url_privacy_statement),
                                ),
                                AboutThisAppData.Url(
                                    text = getString(R.string.about_this_app_accessibility),
                                    url = getString(R.string.url_accessibility),
                                ),
                                AboutThisAppData.Url(
                                    text = getString(R.string.about_this_app_colofon),
                                    url = getString(R.string.about_this_app_colofon_url),
                                ),
                                AboutThisAppData.ClearAppData(
                                    text = getString(R.string.about_this_clear_data)
                                )
                            )
                        )
                    ),
                    configVersionHash = cachedAppConfigUseCase.getCachedAppConfigHash(),
                    configVersionTimestamp = appConfigPersistenceManager.getAppConfigLastFetchedSeconds()
                )

                val actionQrCodeType = MenuFragmentDirections.actionQrCodeType()
                val actionPaperProof = MenuFragmentDirections.actionPaperProof()
                val actionVisitorPass = MenuFragmentDirections.actionVisitorPass()
                val actionAboutThisApp = MenuFragmentDirections.actionAboutThisApp(
                    data = aboutThisAppData
                )

                findNavControllerSafety()?.navigate(
                    MyOverviewTabsFragmentDirections.actionMenu(
                        menuSections = listOf(
                            MenuSection(
                                menuItems = listOf(
                                    MenuSection.MenuItem(
                                        icon = R.drawable.ic_menu_add,
                                        title = R.string.holder_menu_listItem_addVaccinationOrTest_title,
                                        onClick = MenuSection.MenuItem.OnClick.Navigate(
                                            navigationActionId = actionQrCodeType.actionId,
                                            navigationArguments = actionQrCodeType.arguments
                                        )
                                    )
                                )
                            ),
                            MenuSection(
                                menuItems = listOf(
                                    MenuSection.MenuItem(
                                        icon = R.drawable.ic_menu_paper,
                                        title = R.string.add_paper_proof,
                                        onClick = MenuSection.MenuItem.OnClick.Navigate(
                                            navigationActionId = actionPaperProof.actionId,
                                            navigationArguments = actionPaperProof.arguments
                                        )
                                    ),
                                    MenuSection.MenuItem(
                                        icon = R.drawable.ic_menu_briefcase,
                                        title = R.string.holder_menu_visitorpass,
                                        onClick = MenuSection.MenuItem.OnClick.Navigate(
                                            navigationActionId = actionVisitorPass.actionId,
                                            navigationArguments = actionVisitorPass.arguments
                                        )
                                    )
                                )
                            ),
                            MenuSection(
                                menuItems = listOf(
                                    MenuSection.MenuItem(
                                        icon = R.drawable.ic_menu_chatbubble,
                                        title = R.string.frequently_asked_questions,
                                        onClick = MenuSection.MenuItem.OnClick.OpenBrowser(
                                            url = getString(R.string.url_faq)
                                        )
                                    ),
                                    MenuSection.MenuItem(
                                        icon = R.drawable.ic_menu_info,
                                        title = R.string.about_this_app,
                                        onClick = MenuSection.MenuItem.OnClick.Navigate(
                                            navigationActionId = actionAboutThisApp.actionId,
                                            navigationArguments = actionAboutThisApp.arguments
                                        )
                                    )
                                )
                            )
                        ).toTypedArray()
                    )
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getToolbar() = (parentFragment?.parentFragment as HolderMainFragment?)?.getToolbar()
}
