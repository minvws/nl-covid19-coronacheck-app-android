/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.verifier.menu

import android.content.Context
import androidx.lifecycle.MutableLiveData
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigPersistenceManager
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYearTimeNumerical
import nl.rijksoverheid.ctr.design.fragments.menu.MenuFragmentDirections
import nl.rijksoverheid.ctr.design.fragments.menu.MenuSection
import nl.rijksoverheid.ctr.design.menu.MenuViewModel
import nl.rijksoverheid.ctr.design.menu.about.AboutThisAppData
import nl.rijksoverheid.ctr.design.menu.about.HelpdeskData
import nl.rijksoverheid.ctr.shared.livedata.Event
import nl.rijksoverheid.ctr.verifier.BuildConfig
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.usecases.VerifierFeatureFlagUseCase

class MenuViewModelImpl(
    private val featureFlagUseCase: VerifierFeatureFlagUseCase,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase,
    private val appConfigPersistenceManager: AppConfigPersistenceManager
) : MenuViewModel() {
    override fun click(context: Context) {
        (menuSectionLiveData as MutableLiveData).value = Event(menuSections(context))
    }

    private fun getAboutThisAppData(context: Context): AboutThisAppData = AboutThisAppData(
        sections = mutableListOf(
            AboutThisAppData.AboutThisAppSection(
                header = R.string.about_this_app_read_more,
                items = mutableListOf<AboutThisAppData.AboutThisAppItem>(
                    AboutThisAppData.Url(
                        text = context.getString(R.string.privacy_statement),
                        url = context.getString(R.string.url_terms_of_use)
                    ),
                    AboutThisAppData.Url(
                        text = context.getString(R.string.about_this_app_accessibility),
                        url = context.getString(R.string.url_accessibility)
                    ),
                    AboutThisAppData.Url(
                        text = context.getString(R.string.about_this_app_colofon),
                        url = context.getString(R.string.about_this_app_colofon_url)
                    )
                )
            )
        ).apply {
            if (featureFlagUseCase.isVerificationPolicySelectionEnabled()) {
                add(
                    AboutThisAppData.AboutThisAppSection(
                        header = R.string.verifier_about_this_app_law_enforcement,
                        items = listOf(
                            AboutThisAppData.Destination(
                                text = context.getString(R.string.verifier_about_this_app_scan_log),
                                destinationId = R.id.action_scan_log
                            )
                        )
                    )
                )
            }
        }
    )

    private fun menuSections(context: Context): Array<MenuSection> {
        val actionScanInstructions = MenuFragmentDirections.actionScanInstructions()
        val actionPolicySettings = MenuFragmentDirections.actionPolicySettings()
        val actionAboutThisApp = MenuFragmentDirections.actionAboutThisApp(
            data = getAboutThisAppData(context)
        )

        val menuItems = mutableListOf<MenuSection.MenuItem>()

        val howItWorksMenuItem = MenuSection.MenuItem(
            icon = R.drawable.ic_menu_info,
            title = R.string.scan_instructions_menu_title,
            onClick = MenuSection.MenuItem.OnClick.Navigate(
                navigationActionId = actionScanInstructions.actionId,
                navigationArguments = actionScanInstructions.arguments
            )
        )

        val scanSettingsMenuItem = MenuSection.MenuItem(
            icon = R.drawable.ic_menu_qr,
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

        val configFetchDate = OffsetDateTime.ofInstant(
            Instant.ofEpochSecond(appConfigPersistenceManager.getAppConfigLastFetchedSeconds()),
            ZoneOffset.UTC
        ).formatDayMonthYearTimeNumerical()
        val actionHelpdesk = MenuFragmentDirections.actionHelpdesk(
            data = HelpdeskData(
                contactTitle = context.getString(R.string.verifier_helpdesk_contact_title),
                contactMessage = context.getString(R.string.verifier_helpdesk_contact_message),
                supportTitle = context.getString(R.string.verifier_helpdesk_support_title),
                supportMessage = context.getString(R.string.verifier_helpdesk_support_message),
                appVersionTitle = context.getString(R.string.verifier_helpdesk_appVersion),
                appVersion = "${BuildConfig.VERSION_NAME} (build ${BuildConfig.VERSION_CODE})",
                configurationTitle = context.getString(R.string.verifier_helpdesk_configuration),
                configuration = "${cachedAppConfigUseCase.getCachedAppConfigHash()}, $configFetchDate"
            )
        )

        val helpdeskItem = MenuSection.MenuItem(
            icon = R.drawable.ic_menu_helpdesk,
            title = R.string.verifier_helpInfo_helpdesk,
            onClick = MenuSection.MenuItem.OnClick.Navigate(
                navigationActionId = actionHelpdesk.actionId,
                navigationArguments = actionHelpdesk.arguments
            )
        )

        val aboutThisAppMenuItem = MenuSection.MenuItem(
            icon = R.drawable.ic_menu_smartphone,
            title = R.string.about_this_app,
            onClick = MenuSection.MenuItem.OnClick.Navigate(
                navigationActionId = actionAboutThisApp.actionId,
                navigationArguments = actionAboutThisApp.arguments
            )
        )

        menuItems.add(howItWorksMenuItem)
        menuItems.add(supportMenuItem)

        menuItems.add(aboutThisAppMenuItem)

        return listOf(
            MenuSection(
                menuItems = listOfNotNull(
                    howItWorksMenuItem,
                    if (featureFlagUseCase.isVerificationPolicySelectionEnabled()) {
                        scanSettingsMenuItem
                    } else {
                        null
                    }
                )
            ),
            MenuSection(
                menuItems = listOf(
                    supportMenuItem,
                    helpdeskItem
                )
            ),
            MenuSection(
                menuItems = listOf(
                    aboutThisAppMenuItem
                )
            )
        ).toTypedArray()
    }
}
