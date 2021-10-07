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
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import nl.rijksoverheid.ctr.design.BaseMainFragment
import nl.rijksoverheid.ctr.design.ext.styleTitle
import nl.rijksoverheid.ctr.design.menu.about.AboutThisAppData
import nl.rijksoverheid.ctr.design.menu.about.AboutThisAppFragment
import nl.rijksoverheid.ctr.holder.databinding.FragmentMainBinding
import nl.rijksoverheid.ctr.shared.ext.launchUrl
import nl.rijksoverheid.ctr.shared.utils.Accessibility.setAccessibilityFocus

class HolderMainFragment : BaseMainFragment(
    R.layout.fragment_main, setOf(
        R.id.nav_my_overview_tabs,
        R.id.nav_about_this_app,
        R.id.nav_paper_proof_explanation
    )
) {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private var _navController : NavController? = null
    private val navController get() = _navController!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentMainBinding.bind(view)

        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        _navController = navHostFragment.navController

        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        val appBarConfiguration = AppBarConfiguration(
            topLevelDestinations,
            binding.drawerLayout
        )

        val defaultToolbarElevation = resources.getDimension(R.dimen.toolbar_elevation)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.toolbar.elevation = if (destination.id == R.id.nav_my_overview_tabs) {
                0f
            } else {
                defaultToolbarElevation
            }
        }
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        binding.toolbar.setNavigationOnClickListener {
            when (navController.currentDestination?.id) {
                R.id.nav_your_events -> {
                    // Trigger custom dispatcher in destination
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                    return@setNavigationOnClickListener
                }
            }

            NavigationUI.navigateUp(navController, appBarConfiguration)
        }

        binding.toolbar.setOnMenuItemClickListener {
            NavigationUI.onNavDestinationSelected(it, navController)
        }

        binding.navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_frequently_asked_questions -> {
                    getString(R.string.url_faq).launchUrl(requireActivity())
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
                                    ),
                                    AboutThisAppData.ReadMoreItem(
                                        text = getString(R.string.about_this_app_colofon),
                                        url = getString(R.string.about_this_app_colofon_url),
                                    ),
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

    fun resetMenuItemListener(){
        binding.toolbar.setOnMenuItemClickListener {
            NavigationUI.onNavDestinationSelected(it, navController)
        }
    }

    private fun navigationDrawerStyling() {
        val context = binding.navView.context
        binding.navView.menu.findItem(R.id.nav_graph_overview)
            .styleTitle(context, R.attr.textAppearanceHeadline6, heading = true)
        binding.navView.menu.findItem(R.id.nav_settings)
            .styleTitle(context, R.attr.textAppearanceHeadline6, heading = true)
        binding.navView.menu.findItem(R.id.nav_qr_code_type)
            .styleTitle(context, R.attr.textAppearanceHeadline6, heading = true)
        binding.navView.menu.findItem(R.id.nav_about_this_app)
            .styleTitle(context, R.attr.textAppearanceBody1)
        binding.navView.menu.findItem(R.id.nav_frequently_asked_questions)
            .styleTitle(context, R.attr.textAppearanceBody1)
        binding.navView.menu.findItem(R.id.nav_terms_of_use)
            .styleTitle(context, R.attr.textAppearanceBody1)
        binding.navView.menu.findItem(R.id.nav_paper_proof)
            .styleTitle(context, R.attr.textAppearanceBody1)

        // resize drawer according to design
        val width = activity?.resources?.displayMetrics?.widthPixels ?: return
        val layoutParams = binding.navView.layoutParams as DrawerLayout.LayoutParams
        layoutParams.width = (drawerWidthFactor * width).toInt()
        binding.navView.layoutParams = layoutParams
    }

    companion object {
        const val drawerWidthFactor = 0.85f
    }
}
