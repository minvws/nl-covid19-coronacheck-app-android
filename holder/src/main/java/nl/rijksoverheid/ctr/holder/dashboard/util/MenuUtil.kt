/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard.util

import android.content.Context
import nl.rijksoverheid.ctr.design.fragments.menu.MenuFragmentDirections
import nl.rijksoverheid.ctr.design.fragments.menu.MenuSection
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.dashboard.DashboardFragment
import nl.rijksoverheid.ctr.holder.dashboard.DashboardFragmentDirections
import nl.rijksoverheid.ctr.holder.menu.AboutThisAppDataModel
import nl.rijksoverheid.ctr.holder.menu.HelpMenuDataModel
import nl.rijksoverheid.ctr.holder.usecases.HolderFeatureFlagUseCase
import nl.rijksoverheid.ctr.shared.ext.navigateSafety

interface MenuUtil {
    fun showMenu(dashboardFragment: DashboardFragment)
}

class MenuUtilImpl(
    private val aboutThisAppDataModel: AboutThisAppDataModel,
    private val helpMenuDataModel: HelpMenuDataModel,
    private val featureFlagUseCase: HolderFeatureFlagUseCase
) : MenuUtil {

    override fun showMenu(dashboardFragment: DashboardFragment) {
        dashboardFragment.navigateSafety(
            DashboardFragmentDirections.actionMenu(
                toolbarTitle = dashboardFragment.getString(R.string.general_menu),
                menuSections = getMenuSections(dashboardFragment.requireContext())
            )
        )
    }

    fun getMenuSections(context: Context): Array<MenuSection> {
        val actionChooseProofType = MenuFragmentDirections.actionChooseProofType()
        val actionPaperProof = MenuFragmentDirections.actionPaperProof()
        val actionVisitorPass = MenuFragmentDirections.actionVisitorPass()
        val actionAboutThisApp = MenuFragmentDirections.actionAboutThisApp(
            data = aboutThisAppDataModel.get(context)
        )
        val actionSavedEvents = MenuFragmentDirections.actionSavedEvents()
        val actionHelpInfo = MenuFragmentDirections.actionMenu(
            toolbarTitle = context.getString(R.string.holder_helpInfo_title),
            menuSections = helpMenuDataModel.get(context)
        )

        val isVisitorPassEnabled = featureFlagUseCase.getVisitorPassEnabled()

        val addVaccinationOrTestMenuItem = MenuSection.MenuItem(
            icon = R.drawable.ic_menu_add,
            title = R.string.holder_menu_listItem_addVaccinationOrTest_title,
            onClick = MenuSection.MenuItem.OnClick.Navigate(
                navigationActionId = actionChooseProofType.actionId,
                navigationArguments = actionChooseProofType.arguments
            )
        )

        val addPaperProofMenuItem = MenuSection.MenuItem(
            icon = R.drawable.ic_menu_paper,
            title = R.string.holder_menu_paperproof_title,
            subtitle = R.string.holder_menu_paperproof_subTitle,
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

        val savedEventsMenuItem = MenuSection.MenuItem(
            icon = R.drawable.ic_menu_saved_events,
            title = R.string.holder_menu_storedEvents,
            onClick = MenuSection.MenuItem.OnClick.Navigate(
                navigationActionId = actionSavedEvents.actionId
            )
        )

        val helpInfoMenuItem = MenuSection.MenuItem(
            icon = R.drawable.ic_menu_info,
            title = R.string.holder_menu_helpInfo,
            onClick = MenuSection.MenuItem.OnClick.Navigate(
                navigationActionId = actionHelpInfo.actionId,
                navigationArguments = actionHelpInfo.arguments
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

        val menuSections: List<MenuSection> = listOfNotNull(
            if (isVisitorPassEnabled) {
                MenuSection(
                    menuItems = listOf(
                        addVaccinationOrTestMenuItem
                    )
                )
            } else {
                MenuSection(
                    menuItems = listOf(
                        addVaccinationOrTestMenuItem,
                        addPaperProofMenuItem
                    )
                )
            },
            if (isVisitorPassEnabled) {
                MenuSection(
                    menuItems = listOf(
                        addPaperProofMenuItem,
                        addVisitorPassMenuItem
                    )
                )
            } else {
                null
            },
            MenuSection(
                menuItems = listOf(
                    savedEventsMenuItem,
                    helpInfoMenuItem
                )
            )
        )

        return menuSections.toTypedArray()
    }
}
