/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.ui.myoverview.utils

import android.content.Context
import nl.rijksoverheid.ctr.appconfig.api.model.HolderConfig
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigPersistenceManager
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.design.fragments.menu.MenuFragmentDirections
import nl.rijksoverheid.ctr.design.fragments.menu.MenuSection
import nl.rijksoverheid.ctr.design.menu.about.AboutThisAppData
import nl.rijksoverheid.ctr.holder.BuildConfig
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.ui.myoverview.MyOverviewTabsFragment
import nl.rijksoverheid.ctr.holder.ui.myoverview.MyOverviewTabsFragmentDirections
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety

interface MenuUtil {
    fun showMenu(myOverviewTabsFragment: MyOverviewTabsFragment)
}

class MenuUtilImpl(
    private val cachedAppConfigUseCase: CachedAppConfigUseCase,
    private val appConfigPersistenceManager: AppConfigPersistenceManager
): MenuUtil {

    override fun showMenu(myOverviewTabsFragment: MyOverviewTabsFragment) {
        myOverviewTabsFragment.findNavControllerSafety()?.navigate(
            MyOverviewTabsFragmentDirections.actionMenu(
                menuSections = getMenuSections(myOverviewTabsFragment.requireContext())
            )
        )
    }

    fun getMenuSections(context: Context): Array<MenuSection> {
        val actionQrCodeType = MenuFragmentDirections.actionQrCodeType()
        val actionPaperProof = MenuFragmentDirections.actionPaperProof()
        val actionVisitorPass = MenuFragmentDirections.actionVisitorPass()
        val actionAboutThisApp = MenuFragmentDirections.actionAboutThisApp(
            data = getAboutThisAppData(context)
        )

        val isVisitorPassEnabled = (cachedAppConfigUseCase.getCachedAppConfig() as HolderConfig).visitorPassEnabled

        val menuSections = mutableListOf<MenuSection>()

        val addVaccinationOrTestMenuItem = MenuSection.MenuItem(
            icon = R.drawable.ic_menu_add,
            title = R.string.holder_menu_listItem_addVaccinationOrTest_title,
            onClick = MenuSection.MenuItem.OnClick.Navigate(
                navigationActionId = actionQrCodeType.actionId,
                navigationArguments = actionQrCodeType.arguments
            )
        )

        val addPaperProofMenuItem = MenuSection.MenuItem(
            icon = R.drawable.ic_menu_paper,
            title = R.string.add_paper_proof,
            onClick = MenuSection.MenuItem.OnClick.Navigate(
                navigationActionId = actionPaperProof.actionId,
                navigationArguments = actionPaperProof.arguments
            )
        )

        val addVisitorPassMenuItem = MenuSection.MenuItem(
            icon = R.drawable.ic_menu_briefcase,
            title = R.string.holder_menu_visitorpass,
            onClick = MenuSection.MenuItem.OnClick.Navigate(
                navigationActionId = actionVisitorPass.actionId,
                navigationArguments = actionVisitorPass.arguments
            )
        )

        val faqMenuItem = MenuSection.MenuItem(
            icon = R.drawable.ic_menu_chatbubble,
            title = R.string.frequently_asked_questions,
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

        if (isVisitorPassEnabled) {
            menuSections.add(
                MenuSection(
                    menuItems = listOf(
                        addVaccinationOrTestMenuItem
                    )
                )
            )

            menuSections.add(
                MenuSection(
                    menuItems = listOf(
                        addPaperProofMenuItem,
                        addVisitorPassMenuItem
                    )
                )
            )

            menuSections.add(
                MenuSection(
                    menuItems = listOf(
                        faqMenuItem,
                        aboutThisAppMenuItem
                    )
                )
            )
        } else {
            menuSections.add(
                MenuSection(
                    menuItems = listOf(
                        addVaccinationOrTestMenuItem,
                        addPaperProofMenuItem
                    )
                )
            )

            menuSections.add(
                MenuSection(
                    menuItems = listOf(
                        faqMenuItem,
                        aboutThisAppMenuItem
                    )
                )
            )
        }
        return menuSections.toTypedArray()
    }

    private fun getAboutThisAppData(context: Context): AboutThisAppData = AboutThisAppData(
        deeplinkScannerUrl = BuildConfig.DEEPLINK_SCANNER_TEST_URL,
        versionName = BuildConfig.VERSION_NAME,
        versionCode = BuildConfig.VERSION_CODE.toString(),
        sections = listOf(
            AboutThisAppData.AboutThisAppSection(
                header = R.string.about_this_app_read_more,
                items = mutableListOf(
                    AboutThisAppData.Url(
                        text = context.getString(R.string.privacy_statement),
                        url = context.getString(R.string.url_privacy_statement),
                    ),
                    AboutThisAppData.Url(
                        text = context.getString(R.string.about_this_app_accessibility),
                        url = context.getString(R.string.url_accessibility),
                    ),
                    AboutThisAppData.Url(
                        text = context.getString(R.string.about_this_app_colofon),
                        url = context.getString(R.string.about_this_app_colofon_url),
                    ),
                    AboutThisAppData.ClearAppData(
                        text = context.getString(R.string.about_this_clear_data)
                    )
                )
            )
        ),
        configVersionHash = cachedAppConfigUseCase.getCachedAppConfigHash(),
        configVersionTimestamp = appConfigPersistenceManager.getAppConfigLastFetchedSeconds()
    )
}