/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder

import android.os.Bundle
import android.view.WindowManager
import androidx.core.view.GravityCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import nl.rijksoverheid.ctr.appconfig.AppStatusActivity
import nl.rijksoverheid.ctr.appconfig.AppStatusViewModel
import nl.rijksoverheid.ctr.appconfig.model.AppStatus
import nl.rijksoverheid.ctr.holder.databinding.ActivityMainBinding
import nl.rijksoverheid.ctr.shared.BaseActivity
import nl.rijksoverheid.ctr.shared.ext.launchUrl
import nl.rijksoverheid.ctr.shared.ext.styleTitle
import org.koin.androidx.viewmodel.ext.android.viewModel

class HolderMainActivity : BaseActivity(R.id.nav_my_overview) {

    private lateinit var binding: ActivityMainBinding

    private val appStatusViewModel: AppStatusViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)

        if (BuildConfig.FLAVOR == "prod") {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_status,
                R.id.nav_my_overview,
                R.id.nav_settings,
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
                    BuildConfig.URL_FAQ.launchUrl(this)
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
            AppStatusActivity.launch(
                activity = this,
                appStatus = it
            )
        }
    }


    override fun onStart() {
        super.onStart()
        appStatusViewModel.refresh()
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
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.close()
            return
        }
        super.onBackPressed()
    }
}
