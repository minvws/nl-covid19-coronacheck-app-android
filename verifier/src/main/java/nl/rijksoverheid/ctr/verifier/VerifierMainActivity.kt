/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.verifier

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import nl.rijksoverheid.ctr.appconfig.AppStatusFragment
import nl.rijksoverheid.ctr.appconfig.AppStatusViewModel
import nl.rijksoverheid.ctr.appconfig.model.AppStatus
import nl.rijksoverheid.ctr.shared.BaseActivity
import nl.rijksoverheid.ctr.shared.ext.launchUrl
import nl.rijksoverheid.ctr.shared.ext.styleTitle
import nl.rijksoverheid.ctr.verifier.databinding.ActivityMainBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class VerifierMainActivity : BaseActivity(R.id.nav_scan_qr) {

    private lateinit var binding: ActivityMainBinding
    private val appStatusViewModel: AppStatusViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id in arrayOf(
                    R.id.nav_app_status,
                    R.id.nav_onboarding,
                    R.id.nav_privacy_policy
                )
            ) {
                binding.toolbar.visibility = View.GONE
                binding.drawerLayout.setDrawerLockMode(
                    DrawerLayout.LOCK_MODE_LOCKED_CLOSED,
                    GravityCompat.START
                )
            } else {
                binding.toolbar.visibility = View.VISIBLE
                binding.drawerLayout.setDrawerLockMode(
                    DrawerLayout.LOCK_MODE_UNLOCKED,
                    GravityCompat.START
                )
            }
        }

        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_scan_qr, R.id.nav_about_this_app),
            binding.drawerLayout
        )
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        navigationDrawerStyling()

        binding.navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_support -> {
                    BuildConfig.URL_SUPPORT.launchUrl(this)
                }
                R.id.nav_about_this_app -> {
                    BuildConfig.URL_ABOUT_THIS_APP.launchUrl(this)
                }
                R.id.nav_privacy_statement -> {
                    BuildConfig.URL_PRIVACY_STATEMENT.launchUrl(this)
                }
                else -> {
                    NavigationUI.onNavDestinationSelected(item, navController)
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        appStatusViewModel.appStatus.observe(this) {
            if (it !is AppStatus.NoActionRequired) {
                val bundle = bundleOf(AppStatusFragment.EXTRA_APP_STATUS to it)
                navController.navigate(R.id.action_app_status, bundle)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        appStatusViewModel.refresh()
    }

    private fun navigationDrawerStyling() {
        val context = binding.navView.context
        binding.navView.menu.findItem(R.id.nav_scan_qr)
            .styleTitle(context, R.attr.textAppearanceHeadline6)
        binding.navView.menu.findItem(R.id.nav_support)
            .styleTitle(context, R.attr.textAppearanceHeadline6)
        binding.navView.menu.findItem(R.id.nav_about_this_app)
            .styleTitle(context, R.attr.textAppearanceBody1)
        binding.navView.menu.findItem(R.id.nav_give_us_feedback)
            .styleTitle(context, R.attr.textAppearanceBody1)
        binding.navView.menu.findItem(R.id.nav_privacy_statement)
            .styleTitle(context, R.attr.textAppearanceBody1)
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.close()
            return
        }
        super.onBackPressed()
    }
}
