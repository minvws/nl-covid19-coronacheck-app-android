/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.menu

import android.content.Context
import androidx.lifecycle.MutableLiveData
import nl.rijksoverheid.ctr.design.fragments.menu.MenuFragmentDirections
import nl.rijksoverheid.ctr.design.fragments.menu.MenuSection
import nl.rijksoverheid.ctr.design.menu.MenuViewModel
import nl.rijksoverheid.ctr.design.utils.DialogButtonData
import nl.rijksoverheid.ctr.design.utils.DialogFragmentData
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.usecases.HolderFeatureFlagUseCase
import nl.rijksoverheid.ctr.shared.livedata.Event
import nl.rijksoverheid.ctr.shared.models.Environment

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

        val firstSectionItems = listOf(
            addVaccinationOrTestMenuItem,
            addPaperProofMenuItem
        ) + if (isVisitorPassEnabled) {
            listOf(addVisitorPassMenuItem)
        } else {
            emptyList()
        }

        val menuSections: List<MenuSection> = listOfNotNull(
            MenuSection(
                menuItems = firstSectionItems
            ),
            MenuSection(
                menuItems = listOf(
                    savedEventsMenuItem,
                    helpInfoMenuItem
                )
            ),
            if (Environment.get(context) == Environment.Prod) {
                null
            } else {
                val dialogDirection = MenuFragmentDirections.actionDialog(
                    data = DialogFragmentData(
                        title = nl.rijksoverheid.ctr.design.R.string.about_this_app_clear_data_title,
                        message = nl.rijksoverheid.ctr.design.R.string.about_this_app_clear_data_description,
                        positiveButtonData = DialogButtonData.ResetApp(
                            textId = nl.rijksoverheid.ctr.design.R.string.about_this_app_clear_data_confirm
                        ),
                        negativeButtonData = DialogButtonData.Dismiss(nl.rijksoverheid.ctr.design.R.string.about_this_app_clear_data_cancel)
                    )
                )
                MenuSection(
                    menuItems = listOf(
                        MenuSection.MenuItem(
                            icon = R.drawable.ic_warning,
                            color = R.color.error,
                            title = R.string.general_menu_resetApp,
                            onClick = MenuSection.MenuItem.OnClick.Navigate(dialogDirection.actionId, dialogDirection.arguments)
                        )
                    )
                )
            }
        )

        return menuSections.toTypedArray()
    }
}
