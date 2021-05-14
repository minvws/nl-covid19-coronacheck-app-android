/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import nl.rijksoverheid.ctr.design.BaseMainFragment
import nl.rijksoverheid.ctr.design.ext.styleTitle
import nl.rijksoverheid.ctr.design.menu.about.AboutThisAppData
import nl.rijksoverheid.ctr.design.menu.about.AboutThisAppFragment
import nl.rijksoverheid.ctr.holder.databinding.FragmentMainBinding
import nl.rijksoverheid.ctr.holder.ui.myoverview.LocalTestResultViewModel
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.LocalTestResultState
import nl.rijksoverheid.ctr.shared.ext.launchUrl
import nl.rijksoverheid.ctr.shared.ext.setAccessibilityFocus
import nl.rijksoverheid.ctr.shared.ext.show
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class HolderMainFragment : BaseMainFragment(
    R.layout.fragment_main, setOf(
        R.id.nav_my_overview,
        R.id.nav_about_this_app
    )
) {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val localTestResultViewModel: LocalTestResultViewModel by sharedViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentMainBinding.bind(view)

        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        val appBarConfiguration = AppBarConfiguration(
            topLevelDestinations,
            binding.drawerLayout
        )

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        binding.toolbar.setNavigationOnClickListener {
            when (navController.currentDestination?.id) {
                R.id.nav_your_negative_result -> {
                    // Trigger custom dispatcher in destination
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                    return@setNavigationOnClickListener
                }
            }

            NavigationUI.navigateUp(navController, appBarConfiguration)
        }

        binding.navView.setNavigationItemSelectedListener { item ->
            if (appBarConfiguration.topLevelDestinations.contains(R.id.nav_qr_explanation)) {
                appBarConfiguration.topLevelDestinations.remove(R.id.nav_qr_explanation)
            }
            when (item.itemId) {
                R.id.nav_frequently_asked_questions -> {
                    getString(R.string.url_faq).launchUrl(requireActivity())
                }
                R.id.nav_create_qr -> {
                    appBarConfiguration.topLevelDestinations.add(R.id.nav_qr_explanation)
                    navController.navigate(R.id.nav_qr_explanation)
                }
                R.id.nav_about_this_app -> {
                    navController.navigate(
                        R.id.nav_about_this_app, AboutThisAppFragment.getBundle(
                            data = AboutThisAppData(
                                versionName = BuildConfig.VERSION_NAME,
                                versionCode = BuildConfig.VERSION_CODE.toString(),
                                readMoreItems = listOf(
                                    AboutThisAppData.ReadMoreItem(
                                        text = getString(R.string.privacy_statement),
                                        url = getString(R.string.url_privacy_statement),
                                    ),
                                    AboutThisAppData.ReadMoreItem(
                                        text = getString(R.string.about_this_app_accessibility),
                                        url = getString(R.string.url_accessibility),
                                    )
                                )
                            )
                        )
                    )
                }
                else -> {
                    NavigationUI.onNavDestinationSelected(item, navController)
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        navigationDrawerStyling()

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

        localTestResultViewModel.localTestResultStateLiveData.observe(
            viewLifecycleOwner,
            EventObserver { localTestResultState ->
                when (localTestResultState) {
                    is LocalTestResultState.None,
                    is LocalTestResultState.Expired -> {
                        // Nothing
                    }
                    is LocalTestResultState.Valid -> {
                        binding.navView.menu.findItem(R.id.nav_create_qr).title = getString(R.string.create_qr_explanation_menu_title_alternative)
                        navigationDrawerStyling()
                    }
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun presentLoading(loading: Boolean) {
        binding.loading.visibility = if (loading) View.VISIBLE else View.GONE
        if (loading) {
            binding.loading.setAccessibilityFocus()
        } else {
            binding.toolbar.setAccessibilityFocus()
        }
    }

    fun getToolbar(): Toolbar {
        return binding.toolbar
    }

    private fun navigationDrawerStyling() {
        val context = binding.navView.context
        binding.navView.menu.findItem(R.id.nav_my_overview)
            .styleTitle(context, R.attr.textAppearanceHeadline6)
        binding.navView.menu.findItem(R.id.nav_settings)
            .styleTitle(context, R.attr.textAppearanceHeadline6)
        binding.navView.menu.findItem(R.id.nav_about_this_app)
            .styleTitle(context, R.attr.textAppearanceBody1)
        binding.navView.menu.findItem(R.id.nav_frequently_asked_questions)
            .styleTitle(context, R.attr.textAppearanceBody1)
        binding.navView.menu.findItem(R.id.nav_terms_of_use)
            .styleTitle(context, R.attr.textAppearanceBody1)
        binding.navView.menu.findItem(R.id.nav_create_qr)
            .styleTitle(context, R.attr.textAppearanceBody1)
    }

}
