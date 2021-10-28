/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.verifier

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigPersistenceManager
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.design.BaseMainFragment
import nl.rijksoverheid.ctr.design.ext.isScreenReaderOn
import nl.rijksoverheid.ctr.design.ext.styleTitle
import nl.rijksoverheid.ctr.design.menu.about.AboutThisAppData
import nl.rijksoverheid.ctr.design.menu.about.AboutThisAppFragment
import nl.rijksoverheid.ctr.shared.ext.launchUrl
import nl.rijksoverheid.ctr.verifier.databinding.FragmentMainBinding
import org.koin.android.ext.android.inject

class VerifierMainFragment :
    BaseMainFragment(R.layout.fragment_main, setOf(R.id.nav_scan_qr, R.id.nav_about_this_app)) {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private var _navController: NavController? = null
    private val navController get() = _navController!!

    private val cachedAppConfigUseCase: CachedAppConfigUseCase by inject()
    private val appConfigPersistenceManager: AppConfigPersistenceManager by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentMainBinding.bind(view)

        setupWithNavController()

        navigationDrawerStyling()

        binding.navView.setNavigationItemSelectedListener { item ->
            navigationItemSelectedListener(item)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Add close button to menu if user has screenreader enabled
        binding.navView.menu.findItem(R.id.nav_close_menu).isVisible =
            requireActivity().isScreenReaderOn()

        // Close Navigation Drawer when pressing back if it's open
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.close()
                    return
                } else {
                    requireActivity().finishAndRemoveTask()
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupWithNavController() {
        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        _navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            topLevelDestinations,
            binding.drawerLayout
        )

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
    }

    private fun navigationDrawerStyling() {
        val context = binding.navView.context
        binding.navView.menu.findItem(R.id.nav_scan_qr)
            .styleTitle(context, R.attr.textAppearanceHeadline6)
        binding.navView.menu.findItem(R.id.nav_scan_instructions)
            .styleTitle(context, R.attr.textAppearanceHeadline6)
        binding.navView.menu.findItem(R.id.nav_support)
            .styleTitle(context, R.attr.textAppearanceBody1)
        binding.navView.menu.findItem(R.id.nav_about_this_app)
            .styleTitle(context, R.attr.textAppearanceBody1)
        binding.navView.menu.findItem(R.id.nav_give_us_feedback)
            .styleTitle(context, R.attr.textAppearanceBody1)
        binding.navView.menu.findItem(R.id.nav_close_menu)
            .styleTitle(context, R.attr.textAppearanceBody1)
    }

    private fun navigationItemSelectedListener(item: MenuItem) {
        when (item.itemId) {
            R.id.nav_support -> {
                getString(R.string.url_faq).launchUrl(requireActivity())
            }
            R.id.nav_about_this_app -> {
                navController.navigate(
                    R.id.action_about_this_app, AboutThisAppFragment.getBundle(
                        data = AboutThisAppData(
                            versionName = BuildConfig.VERSION_NAME,
                            versionCode = BuildConfig.VERSION_CODE.toString(),
                            readMoreItems = listOf(
                                AboutThisAppData.Url(
                                    text = getString(R.string.privacy_statement),
                                    url = getString(R.string.url_terms_of_use),
                                ),
                                AboutThisAppData.Url(
                                    text = getString(R.string.about_this_app_accessibility),
                                    url = getString(R.string.url_accessibility),
                                ),
                                AboutThisAppData.Url(
                                    text = getString(R.string.about_this_app_colofon),
                                    url = getString(R.string.about_this_app_colofon_url),
                                ),
                            ),
                            configVersionHash = cachedAppConfigUseCase.getCachedAppConfigHash(),
                            configVersionTimestamp = appConfigPersistenceManager.getAppConfigLastFetchedSeconds()
                        )
                    )
                )
            }
            R.id.nav_close_menu -> {
                binding.navView.menu.close()
            }
            else -> {
                NavigationUI.onNavDestinationSelected(item, navController)
            }
        }
    }

    fun getToolbar(): Toolbar {
        return binding.toolbar
    }
}
