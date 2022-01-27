/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.verifier.ui.scanqr.util

import android.content.Context
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigPersistenceManager
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.FeatureFlagUseCase
import nl.rijksoverheid.ctr.design.fragments.menu.MenuFragmentDirections
import nl.rijksoverheid.ctr.design.fragments.menu.MenuSection
import nl.rijksoverheid.ctr.design.menu.about.AboutThisAppData
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.verifier.BuildConfig
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.ui.scanqr.ScanQrFragment
import nl.rijksoverheid.ctr.verifier.ui.scanqr.ScanQrFragmentDirections

interface MenuUtil {
    fun showMenu(scanQrFragment: ScanQrFragment)
}

class MenuUtilImpl(
    private val cachedAppConfigUseCase: CachedAppConfigUseCase,
    private val appConfigPersistenceManager: AppConfigPersistenceManager,
    private val featureFlagUseCase: FeatureFlagUseCase
): MenuUtil {

    override fun showMenu(scanQrFragment: ScanQrFragment) {
        scanQrFragment.findNavControllerSafety()?.navigate(
            ScanQrFragmentDirections.actionMenu(
                menuSections = getMenuSections(scanQrFragment.requireContext())
            )
        )
    }

    fun getMenuSections(context: Context): Array<MenuSection> {
        val actionScanInstructions = MenuFragmentDirections.actionScanInstructions()
        val actionPolicySettings = MenuFragmentDirections.actionPolicySettings()
        val actionAboutThisApp = MenuFragmentDirections.actionAboutThisApp(
            data = getAboutThisAppData(context)
        )

        val isVerificationPolicyEnabled = featureFlagUseCase.isVerificationPolicySelectionEnabled()

        val menuItems = mutableListOf<MenuSection.MenuItem>()

        val howItWorksMenuItem = MenuSection.MenuItem(
            icon = R.drawable.ic_menu_paper,
            title = R.string.scan_instructions_menu_title,
            onClick = MenuSection.MenuItem.OnClick.Navigate(
                navigationActionId = actionScanInstructions.actionId,
                navigationArguments = actionScanInstructions.arguments
            )
        )

        val scanSettingsMenuItem = MenuSection.MenuItem(
            icon = R.drawable.ic_menu_paper,
            title = R.string.verifier_menu_risksetting,
            onClick = MenuSection.MenuItem.OnClick.Navigate(
                navigationActionId = actionPolicySettings.actionId,
                navigationArguments = actionPolicySettings.arguments
            )
        )

        val supportMenuItem = MenuSection.MenuItem(
            icon = R.drawable.ic_menu_chatbubble,
            title = R.string.support,
            onClick = MenuSection.MenuItem.OnClick.OpenBrowser(
                url = context.getString(R.string.url_faq)
            )
        )

        val aboutThisAppMenuItem = MenuSection.MenuItem(
            icon = R.drawable.ic_menu_info,
            title = R.string.about_this_app,
            onClick = MenuSection.MenuItem.OnClick.Navigate(
                navigationActionId = actionAboutThisApp.actionId,
                navigationArguments = actionAboutThisApp.arguments
            )
        )

        menuItems.add(howItWorksMenuItem)
        menuItems.add(supportMenuItem)
        if (featureFlagUseCase.isVerificationPolicySelectionEnabled()) {
            menuItems.add(scanSettingsMenuItem)
        }
        menuItems.add(aboutThisAppMenuItem)

        return listOf(
            MenuSection(
                menuItems = menuItems
            )
        ).toTypedArray()
    }

    private fun getAboutThisAppData(context: Context): AboutThisAppData = AboutThisAppData(
        versionName = BuildConfig.VERSION_NAME,
        versionCode = BuildConfig.VERSION_CODE.toString(),
        sections = mutableListOf(
            AboutThisAppData.AboutThisAppSection(
                header = R.string.about_this_app_read_more,
                items = mutableListOf<AboutThisAppData.AboutThisAppItem>(
                    AboutThisAppData.Url(
                        text = context.getString(R.string.privacy_statement),
                        url = context.getString(R.string.url_terms_of_use),
                    ),
                    AboutThisAppData.Url(
                        text = context.getString(R.string.about_this_app_accessibility),
                        url = context.getString(R.string.url_accessibility),
                    ),
                    AboutThisAppData.Url(
                        text = context.getString(R.string.about_this_app_colofon),
                        url = context.getString(R.string.about_this_app_colofon_url),
                    )
                ).apply {
                    if (!BuildConfig.FLAVOR.lowercase().contains("prod")) {
                        add(AboutThisAppData.ClearAppData(
                            text = context.getString(R.string.about_this_clear_data)
                        ))
                    }
                }
            )
        ).apply {
            if (featureFlagUseCase.isVerificationPolicySelectionEnabled()) {
                add(AboutThisAppData.AboutThisAppSection(
                    header = R.string.verifier_about_this_app_law_enforcement,
                    items = listOf(
                        AboutThisAppData.Destination(
                            text = context.getString(R.string.verifier_about_this_app_scan_log),
                            destinationId = R.id.action_scan_log
                        )
                    )
                ))
            }
        },
        configVersionHash = cachedAppConfigUseCase.getCachedAppConfigHash(),
        configVersionTimestamp = appConfigPersistenceManager.getAppConfigLastFetchedSeconds()
    )
}