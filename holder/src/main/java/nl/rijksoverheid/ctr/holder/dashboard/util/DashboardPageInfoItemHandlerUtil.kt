/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard.util

import android.content.Context
import android.content.Intent
import android.provider.Settings
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import nl.rijksoverheid.ctr.design.ext.formatDayMonthTime
import nl.rijksoverheid.ctr.design.fragments.info.ButtonData
import nl.rijksoverheid.ctr.design.fragments.info.DescriptionData
import nl.rijksoverheid.ctr.design.fragments.info.InfoFragmentData
import nl.rijksoverheid.ctr.design.fragments.info.InfoFragmentDirections
import nl.rijksoverheid.ctr.design.utils.InfoFragmentUtil
import nl.rijksoverheid.ctr.design.utils.IntentUtil
import nl.rijksoverheid.ctr.holder.MainNavDirections
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.dashboard.DashboardPageFragment
import nl.rijksoverheid.ctr.holder.dashboard.items.DashboardInfoCardAdapterItem
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardItem
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteOriginType
import nl.rijksoverheid.ctr.persistence.HolderCachedAppConfigUseCase
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.persistence.database.entities.RemovedEventEntity
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy

/**
 * Handles [DashboardInfoCardAdapterItem] actions
 */
interface DashboardPageInfoItemHandlerUtil {
    fun handleButtonClick(
        dashboardPageFragment: DashboardPageFragment,
        infoItem: DashboardItem.InfoItem
    )

    fun handleDismiss(
        dashboardPageFragment: DashboardPageFragment,
        infoCardAdapterItem: DashboardInfoCardAdapterItem,
        infoItem: DashboardItem.InfoItem
    )
}

class DashboardPageInfoItemHandlerUtilImpl(
    private val infoFragmentUtil: InfoFragmentUtil,
    private val intentUtil: IntentUtil,
    private val cachedAppConfigUseCase: HolderCachedAppConfigUseCase,
    private val removedEventsBottomSheetUtil: RemovedEventsBottomSheetUtil
) : DashboardPageInfoItemHandlerUtil {

    /**
     * Handles the button click in the info card
     */
    override fun handleButtonClick(
        dashboardPageFragment: DashboardPageFragment,
        infoItem: DashboardItem.InfoItem
    ) {
        when (infoItem) {
            is DashboardItem.InfoItem.ConfigFreshnessWarning ->
                onConfigRefreshClicked(dashboardPageFragment, infoItem)
            is DashboardItem.InfoItem.ClockDeviationItem ->
                onClockDeviationClicked(dashboardPageFragment)
            is DashboardItem.InfoItem.OriginInfoItem ->
                onOriginInfoClicked(dashboardPageFragment, infoItem)
            is DashboardItem.InfoItem.MissingDutchVaccinationItem ->
                onMissingDutchVaccinationItemClicked(dashboardPageFragment)
            is DashboardItem.InfoItem.AppUpdate -> openPlayStore(dashboardPageFragment)
            is DashboardItem.InfoItem.VisitorPassIncompleteItem -> {
                onVisitorPassIncompleteClicked(dashboardPageFragment)
            }
            is DashboardItem.InfoItem.DisclosurePolicyItem -> {
                onDisclosurePolicyItemClicked(
                    dashboardPageFragment.requireContext(),
                    infoItem.disclosurePolicy
                )
            }
            is DashboardItem.InfoItem.BlockedEvents -> onBlockedEventsClick(dashboardPageFragment, infoItem.blockedEvents)
            is DashboardItem.InfoItem.FuzzyMatchedEvents -> onFuzzyMatchedEventsClick(dashboardPageFragment, infoItem.storedEvent, infoItem.events)
            is DashboardItem.InfoItem.GreenCardExpiredItem -> {
                /* nothing, DashboardPageFragment.setItems never creates a card for this */
            }
        }
    }

    private fun onDisclosurePolicyItemClicked(
        context: Context,
        disclosurePolicy: DisclosurePolicy
    ) {
        val urlResource = when (disclosurePolicy) {
            DisclosurePolicy.OneG -> R.string.holder_dashboard_only1GaccessBanner_link
            DisclosurePolicy.ThreeG -> R.string.holder_dashboard_only3GaccessBanner_link
            DisclosurePolicy.OneAndThreeG -> R.string.holder_dashboard_3Gand1GaccessBanner_link
            DisclosurePolicy.ZeroG -> R.string.holder_dashboard_noDomesticCertificatesBanner_url
        }
        intentUtil.openUrl(context, context.getString(urlResource))
    }

    private fun onDomesticVaccinationExpiredItemClicked(
        dashboardPageFragment: DashboardPageFragment
    ) {
        val navigationDirection = MainNavDirections.actionGetEvents(
            toolbarTitle = dashboardPageFragment.getString(R.string.holder_addVaccination_title),
            originType = RemoteOriginType.Vaccination
        )

        infoFragmentUtil.presentAsBottomSheet(
            dashboardPageFragment.parentFragmentManager,
            InfoFragmentData.TitleDescriptionWithButton(
                title = dashboardPageFragment.getString(R.string.holder_expiredDomesticVaccinationModal_title),
                descriptionData = DescriptionData(
                    R.string.holder_expiredDomesticVaccinationModal_body,
                    htmlLinksEnabled = true
                ),
                secondaryButtonData = ButtonData.NavigationButton(
                    text = dashboardPageFragment.getString(R.string.holder_expiredDomesticVaccinationModal_button_addBoosterVaccination),
                    navigationActionId = navigationDirection.actionId,
                    navigationArguments = navigationDirection.arguments
                )
            )
        )
    }

    private fun onDomesticVaccinationAssessmentExpiredClicked(
        dashboardPageFragment: DashboardPageFragment
    ) {
        val descriptionText = dashboardPageFragment.getString(R.string.holder_dashboard_visitorpassexpired_body,
            cachedAppConfigUseCase.getCachedAppConfig().vaccinationAssessmentEventValidityDays)

        infoFragmentUtil.presentAsBottomSheet(
            dashboardPageFragment.parentFragmentManager,
            InfoFragmentData.TitleDescriptionWithButton(
                title = dashboardPageFragment.getString(R.string.holder_dashboard_visitorpassexpired_title),
                descriptionData = DescriptionData(
                    htmlTextString = descriptionText,
                    htmlLinksEnabled = true
                )
            )
        )
    }

    private fun openPlayStore(dashboardPageFragment: DashboardPageFragment) {
        intentUtil.openPlayStore(dashboardPageFragment.requireContext())
    }

    private fun onMissingDutchVaccinationItemClicked(dashboardPageFragment: DashboardPageFragment) {
        infoFragmentUtil.presentAsBottomSheet(
            dashboardPageFragment.parentFragmentManager,
            InfoFragmentData.TitleDescription(
                title = dashboardPageFragment.getString(R.string.missing_dutch_certificate_title),
                descriptionData = DescriptionData(
                    htmlText = R.string.holder_incompletedutchvaccination_paragraph_secondvaccine,
                    htmlLinksEnabled = true
                )
            )
        )
    }

    private fun onConfigRefreshClicked(
        dashboardPageFragment: DashboardPageFragment,
        infoItem: DashboardItem.InfoItem.ConfigFreshnessWarning
    ) {
        infoFragmentUtil.presentAsBottomSheet(
            dashboardPageFragment.parentFragmentManager,
            InfoFragmentData.TitleDescription(
                title = dashboardPageFragment.getString(R.string.config_warning_page_title),
                descriptionData = DescriptionData(
                    htmlTextString = dashboardPageFragment.getString(
                        R.string.config_warning_page_message,
                        OffsetDateTime.ofInstant(
                            Instant.ofEpochSecond(infoItem.maxValidityDate),
                            ZoneOffset.UTC
                        ).formatDayMonthTime(dashboardPageFragment.requireContext())
                    ),
                    htmlLinksEnabled = true
                )
            )
        )
    }

    private fun onClockDeviationClicked(
        dashboardPageFragment: DashboardPageFragment
    ) {
        infoFragmentUtil.presentAsBottomSheet(
            dashboardPageFragment.parentFragmentManager, InfoFragmentData.TitleDescription(
                title = dashboardPageFragment.getString(R.string.clock_deviation_explanation_title),
                descriptionData = DescriptionData(
                    R.string.clock_deviation_explanation_description,
                    customLinkIntent = Intent(Settings.ACTION_DATE_SETTINGS)
                )
            )
        )
    }

    private fun onTestCertificate3GValidityClicked(dashboardPageFragment: DashboardPageFragment) {
        infoFragmentUtil.presentAsBottomSheet(
            dashboardPageFragment.parentFragmentManager,
            InfoFragmentData.TitleDescription(
                title = dashboardPageFragment.getString(R.string.holder_my_overview_3g_test_validity_bottom_sheet_title),
                descriptionData = DescriptionData(
                    R.string.holder_my_overview_3g_test_validity_bottom_sheet_body,
                    htmlLinksEnabled = true
                )
            )
        )
    }

    private fun onVisitorPassIncompleteClicked(dashboardPageFragment: DashboardPageFragment) {
        val navigationDirection = InfoFragmentDirections.actionCommercialTestInputToken()

        infoFragmentUtil.presentFullScreen(
            currentFragment = dashboardPageFragment,
            toolbarTitle = dashboardPageFragment.getString(R.string.holder_completecertificate_toolbar),
            data = InfoFragmentData.TitleDescriptionWithButton(
                title = dashboardPageFragment.getString(R.string.holder_completecertificate_title),
                descriptionData = DescriptionData(
                    htmlText = R.string.holder_completecertificate_body,
                    htmlLinksEnabled = true
                ),
                primaryButtonData = ButtonData.NavigationButton(
                    text = dashboardPageFragment.getString(R.string.holder_completecertificate_button_fetchnegativetest),
                    navigationActionId = navigationDirection.actionId,
                    navigationArguments = navigationDirection.arguments
                )
            )
        )
    }

    private fun onBlockedEventsClick(dashboardPageFragment: DashboardPageFragment, blockedEvents: List<RemovedEventEntity>) {
        removedEventsBottomSheetUtil.presentBlockedEvents(dashboardPageFragment, blockedEvents)
    }

    private fun onFuzzyMatchedEventsClick(dashboardPageFragment: DashboardPageFragment, storedEvent: EventGroupEntity, events: List<RemovedEventEntity>) {
        removedEventsBottomSheetUtil.presentRemovedEvents(dashboardPageFragment, storedEvent, events)
    }

    private fun onOriginInfoClicked(
        dashboardPageFragment: DashboardPageFragment,
        item: DashboardItem.InfoItem.OriginInfoItem
    ) {
        when (item.greenCardType) {
            is GreenCardType.Eu -> presentOriginInfoForEuQr(
                item.originType, dashboardPageFragment
            )
        }
    }

    private fun presentOriginInfoForEuQr(
        originType: OriginType,
        dashboardPageFragment: DashboardPageFragment
    ) {
        infoFragmentUtil.presentAsBottomSheet(
            dashboardPageFragment.parentFragmentManager,
            data = when (originType) {
                is OriginType.Test -> {
                    InfoFragmentData.TitleDescription(
                        title = dashboardPageFragment.getString(R.string.my_overview_green_card_not_valid_title_test),
                        descriptionData = DescriptionData(R.string.my_overview_green_card_not_valid_eu_but_is_in_domestic_bottom_sheet_description_test)
                    )
                }
                is OriginType.Vaccination -> {
                    InfoFragmentData.TitleDescription(
                        title = dashboardPageFragment.getString(R.string.my_overview_green_card_not_valid_title_vaccination),
                        descriptionData = DescriptionData(R.string.my_overview_green_card_not_valid_eu_but_is_in_domestic_bottom_sheet_description_vaccination)
                    )
                }
                is OriginType.Recovery -> {
                    InfoFragmentData.TitleDescription(
                        title = dashboardPageFragment.getString(R.string.my_overview_green_card_not_valid_title_recovery),
                        descriptionData = DescriptionData(
                            htmlText = R.string.my_overview_green_card_not_valid_eu_but_is_in_domestic_bottom_sheet_description_recovery,
                            htmlLinksEnabled = true
                        )
                    )
                }
                is OriginType.VaccinationAssessment -> {
                    InfoFragmentData.TitleDescription(
                        title = dashboardPageFragment.getString(R.string.holder_notvalidinthisregionmodal_visitorpass_international_title),
                        descriptionData = DescriptionData(
                            htmlText = R.string.holder_notvalidinthisregionmodal_visitorpass_international_body,
                            htmlLinksEnabled = true)
                    )
                }
            }
        )
    }

    private fun presentOriginInfoForDomesticQr(
        originType: OriginType,
        dashboardPageFragment: DashboardPageFragment
    ) {
        val (title, description) = when (originType) {
            OriginType.Test -> Pair(
                dashboardPageFragment.getString(R.string.my_overview_green_card_not_valid_title_test),
                R.string.my_overview_green_card_not_valid_domestic_but_is_in_eu_bottom_sheet_description_test
            )
            OriginType.Vaccination -> Pair(
                dashboardPageFragment.getString(R.string.my_overview_green_card_not_valid_title_vaccination),
                R.string.holder_addVaccination_message
            )
            OriginType.Recovery -> Pair(
                dashboardPageFragment.getString(R.string.my_overview_green_card_not_valid_title_recovery),
                R.string.my_overview_green_card_not_valid_domestic_but_is_in_eu_bottom_sheet_description_recovery
            )
            OriginType.VaccinationAssessment -> Pair(
                // Missing domestic visitor pass can never happen
                dashboardPageFragment.getString(R.string.holder_notvalidinthisregionmodal_visitorpass_international_title),
                R.string.holder_notvalidinthisregionmodal_visitorpass_international_body
            )
        }
        infoFragmentUtil.presentAsBottomSheet(
            dashboardPageFragment.parentFragmentManager,
            InfoFragmentData.TitleDescription(
                title = title,
                descriptionData = DescriptionData(description, htmlLinksEnabled = true)
            )
        )
    }

    /**
     * Handles the dismiss button click in the info card
     */
    override fun handleDismiss(
        dashboardPageFragment: DashboardPageFragment,
        infoCardAdapterItem: DashboardInfoCardAdapterItem,
        infoItem: DashboardItem.InfoItem
    ) {
        // Remove section from adapter
        dashboardPageFragment.section.remove(infoCardAdapterItem)

        // Clear preference so it doesn't show again
        when (infoItem) {
            is DashboardItem.InfoItem.GreenCardExpiredItem -> {
                dashboardPageFragment.dashboardViewModel.removeOrigin(infoItem.originEntity)
            }
            is DashboardItem.InfoItem.DisclosurePolicyItem -> {
                dashboardPageFragment.dashboardViewModel.dismissPolicyInfo(infoItem.disclosurePolicy)
            }
            is DashboardItem.InfoItem.BlockedEvents -> {
                dashboardPageFragment.dashboardViewModel.dismissBlockedEventsInfo()
            }
            is DashboardItem.InfoItem.FuzzyMatchedEvents -> {
                dashboardPageFragment.dashboardViewModel.dismissFuzzyMatchedEventsInfo()
            }
            else -> {
            }
        }
    }
}
