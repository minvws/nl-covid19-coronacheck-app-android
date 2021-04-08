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
import androidx.core.view.GravityCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import nl.rijksoverheid.ctr.design.BaseMainFragment
import nl.rijksoverheid.ctr.design.ext.isScreenReaderOn
import nl.rijksoverheid.ctr.design.menu.about.AboutThisAppData
import nl.rijksoverheid.ctr.design.menu.about.AboutThisAppFragment
import nl.rijksoverheid.ctr.holder.databinding.FragmentMainBinding
import nl.rijksoverheid.ctr.shared.ext.launchUrl
import nl.rijksoverheid.ctr.shared.ext.setAccessibilityFocus
import nl.rijksoverheid.ctr.shared.ext.styleTitle

class HolderMainFragment : BaseMainFragment(R.layout.fragment_main) {

    private var _binding: FragmentMainBinding? = null
    private val binding: FragmentMainBinding by lazy { _binding!! }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentMainBinding.bind(view)

        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_my_overview,
                R.id.nav_about_this_app
            ),
            binding.drawerLayout
        )

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        navigationDrawerStyling()

        binding.navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_frequently_asked_questions -> {
                    BuildConfig.URL_FAQ.launchUrl(requireActivity())
                }
                R.id.nav_about_this_app -> {
                    navController.navigate(
                        R.id.nav_about_this_app, AboutThisAppFragment.getBundle(
                            data = AboutThisAppData(
                                versionName = BuildConfig.VERSION_NAME,
                                versionCode = BuildConfig.VERSION_CODE.toString()
                            )
                        )
                    )
                }
                R.id.nav_privacy_statement -> {
                    BuildConfig.URL_PRIVACY_STATEMENT.launchUrl(requireActivity())
                }
                R.id.nav_close_menu -> {
                    binding.navView.menu.close()
                }
                else -> {
                    NavigationUI.onNavDestinationSelected(item, navController)
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Add close button to menu if user has screenreader enabled
        binding.navView.menu.findItem(R.id.nav_close_menu).isVisible =
            requireActivity().isScreenReaderOn()
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

    private fun navigationDrawerStyling() {
        val context = binding.navView.context
        binding.navView.menu.findItem(R.id.nav_home)
            .styleTitle(context, R.attr.textAppearanceHeadline6)
        binding.navView.menu.findItem(R.id.nav_settings)
            .styleTitle(context, R.attr.textAppearanceHeadline6)
        binding.navView.menu.findItem(R.id.nav_about_this_app)
            .styleTitle(context, R.attr.textAppearanceBody1)
        binding.navView.menu.findItem(R.id.nav_frequently_asked_questions)
            .styleTitle(context, R.attr.textAppearanceBody1)
        binding.navView.menu.findItem(R.id.nav_privacy_statement)
            .styleTitle(context, R.attr.textAppearanceBody1)
        binding.navView.menu.findItem(R.id.nav_terms_of_use)
            .styleTitle(context, R.attr.textAppearanceBody1)
        binding.navView.menu.findItem(R.id.nav_close_menu)
            .styleTitle(context, R.attr.textAppearanceBody1)
    }
}
