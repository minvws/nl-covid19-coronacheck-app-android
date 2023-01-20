/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.menu

import android.content.Context
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigPersistenceManager
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYearTimeNumerical
import nl.rijksoverheid.ctr.design.fragments.menu.MenuFragmentDirections
import nl.rijksoverheid.ctr.design.fragments.menu.MenuSection
import nl.rijksoverheid.ctr.design.menu.about.HelpdeskData
import nl.rijksoverheid.ctr.holder.BuildConfig
import nl.rijksoverheid.ctr.holder.R

interface HelpMenuDataModel {
    fun get(context: Context): Array<MenuSection>
}

class HelpMenuDataModelImpl(
    private val aboutThisAppDataModel: AboutThisAppDataModel,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase,
    private val appConfigPersistenceManager: AppConfigPersistenceManager
) : HelpMenuDataModel {

    override fun get(context: Context): Array<MenuSection> {
        val configFetchDate = OffsetDateTime.ofInstant(
            Instant.ofEpochSecond(appConfigPersistenceManager.getAppConfigLastFetchedSeconds()),
            ZoneOffset.UTC
        ).formatDayMonthYearTimeNumerical()
        val actionHelpdesk = MenuFragmentDirections.actionHelpdesk(
            data = HelpdeskData(
                contactTitle = context.getString(R.string.holder_helpdesk_contact_title),
                contactMessage = context.getString(R.string.holder_helpdesk_contact_message),
                supportTitle = context.getString(R.string.holder_helpdesk_support_title),
                supportMessage = context.getString(R.string.holder_helpdesk_support_message),
                appVersionTitle = context.getString(R.string.holder_helpdesk_appVersion),
                appVersion = "${BuildConfig.VERSION_NAME} (build ${BuildConfig.VERSION_CODE})",
                configurationTitle = context.getString(R.string.holder_helpdesk_configuration),
                configuration = "${cachedAppConfigUseCase.getCachedAppConfigHash()}, $configFetchDate"
            )
        )

        val aboutThisAppAction = MenuFragmentDirections.actionAboutThisApp(
            data = aboutThisAppDataModel.get(context)
        )
        return listOf(
            MenuSection(
                menuItems = listOf(
                    MenuSection.MenuItem(
                        icon = R.drawable.ic_menu_chatbubble,
                        title = R.string.frequently_asked_questions,
                        onClick = MenuSection.MenuItem.OnClick.OpenBrowser(
                            url = context.getString(R.string.url_faq)
                        )
                    ),
                    MenuSection.MenuItem(
                        icon = R.drawable.ic_menu_helpdesk,
                        title = R.string.holder_helpInfo_helpdesk,
                        onClick = MenuSection.MenuItem.OnClick.Navigate(
                            navigationActionId = actionHelpdesk.actionId,
                            navigationArguments = actionHelpdesk.arguments
                        )
                    )
                )
            ),
            MenuSection(
                menuItems = listOf(
                    MenuSection.MenuItem(
                        icon = R.drawable.ic_menu_smartphone,
                        title = R.string.about_this_app,
                        onClick = MenuSection.MenuItem.OnClick.Navigate(
                            navigationActionId = aboutThisAppAction.actionId,
                            navigationArguments = aboutThisAppAction.arguments
                        )
                    )
                )
            )
        ).toTypedArray()
    }
}
