/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.menu

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.rijksoverheid.ctr.design.fragments.menu.MenuFragmentDirections
import nl.rijksoverheid.ctr.design.fragments.menu.MenuSection
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.usecases.HolderFeatureFlagUseCase
import nl.rijksoverheid.ctr.shared.livedata.Event
import nl.rijksoverheid.ctr.shared.models.Environment

abstract class MenuViewModel : ViewModel() {
    val menuSectionLiveData: LiveData<Event<Array<MenuSection>>> = MutableLiveData()
    abstract fun click(context: Context)
}

class MenuViewModelImpl(
    private val helpMenuDataModel: HelpMenuDataModel,
    private val featureFlagUseCase: HolderFeatureFlagUseCase
) : MenuViewModel() {

    override fun click(context: Context) {
        (menuSectionLiveData as MutableLiveData).value = Event(menuSections(context))
    }

    private fun menuSections(context: Context): Array<MenuSection> {
        val actionChooseProofType = MenuFragmentDirections.actionChooseProofType()
        val actionPaperProof = MenuFragmentDirections.actionPaperProof()
        val actionVisitorPass = MenuFragmentDirections.actionVisitorPass()
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
            ),
            if (Environment.get(context) == Environment.Prod) {
                null
            } else {
                MenuSection(
                    menuItems = listOf(
                        MenuSection.MenuItem(
                            icon = R.drawable.ic_menu_info,
                            color = R.color.error,
                            title = R.string.holder_menu_resetApp,
                            onClick = MenuSection.MenuItem.OnClick.ResetApp
                        )
                    )
                )
            }
        )

        return menuSections.toTypedArray()
    }
}
