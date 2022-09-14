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
import nl.rijksoverheid.ctr.design.ext.formatDateTime
import nl.rijksoverheid.ctr.design.ext.formatDayMonthTime
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYear
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
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEvent
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventNegativeTest
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventPositiveTest
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventRecovery
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccination
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccinationAssessment
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteOriginType
import nl.rijksoverheid.ctr.holder.models.HolderFlow
import nl.rijksoverheid.ctr.holder.models.HolderStep
import nl.rijksoverheid.ctr.holder.your_events.utils.RemoteEventUtil
import nl.rijksoverheid.ctr.persistence.HolderCachedAppConfigUseCase
import nl.rijksoverheid.ctr.persistence.database.entities.BlockedEventEntity
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.shared.ext.capitalize
import nl.rijksoverheid.ctr.shared.ext.launchUrl
import nl.rijksoverheid.ctr.shared.factories.ErrorCodeStringFactory
import nl.rijksoverheid.ctr.shared.models.AppErrorResult
import nl.rijksoverheid.ctr.shared.models.BlockedEventException
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
    private val remoteEventUtil: RemoteEventUtil,
    private val errorCodeStringFactory: ErrorCodeStringFactory
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
            is DashboardItem.InfoItem.DomesticVaccinationExpiredItem -> {
                onDomesticVaccinationExpiredItemClicked(dashboardPageFragment)
            }
            is DashboardItem.InfoItem.DomesticVaccinationAssessmentExpiredItem -> {
                onDomesticVaccinationAssessmentExpiredClicked(dashboardPageFragment)
            }
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
        context.getString(urlResource).launchUrl(context)
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

    private fun onBlockedEventsClick(dashboardPageFragment: DashboardPageFragment, blockedEvents: List<BlockedEventEntity>) {
        val context = dashboardPageFragment.requireContext()
        val removedEventsHtml = StringBuilder()
        blockedEvents.forEachIndexed { index, blockedEvent ->
            val remoteEventClass = RemoteEvent.getRemoteEventClassFromType(blockedEvent.type)

            val name = when (remoteEventClass) {
                RemoteEventVaccination::class.java -> context.getString(R.string.general_vaccination).capitalize()
                RemoteEventNegativeTest::class.java -> context.getString(R.string.general_negativeTest).capitalize()
                RemoteEventPositiveTest::class.java -> context.getString(R.string.general_positiveTest).capitalize()
                RemoteEventRecovery::class.java -> context.getString(R.string.general_recoverycertificate)
                RemoteEventVaccinationAssessment::class.java -> context.getString(R.string.general_visitorPass)
                else -> ""
            }
            val date = when (remoteEventClass) {
                RemoteEventVaccination::class.java -> "${context.getString(R.string.qr_card_vaccination_title_eu)} ${blockedEvent.eventTime?.toLocalDate()?.formatDayMonthYear()}"
                RemoteEventNegativeTest::class.java -> "${context.getString(R.string.qr_card_test_title_eu)} ${blockedEvent.eventTime?.formatDateTime(context)}"
                RemoteEventPositiveTest::class.java -> "${context.getString(R.string.qr_card_test_title_eu)} ${blockedEvent.eventTime?.formatDateTime(context)}"
                RemoteEventRecovery::class.java -> "${context.getString(R.string.qr_card_recovery_title_eu)} ${blockedEvent.eventTime?.toLocalDate()?.formatDayMonthYear()}"
                RemoteEventVaccinationAssessment::class.java -> "${context.getString(R.string.holder_event_vaccination_assessment_about_date)} ${blockedEvent.eventTime?.formatDateTime(context)}"
                else -> ""
            }

            removedEventsHtml.append("<b>$name</b>")
            removedEventsHtml.append("<br/>")
            removedEventsHtml.append("<b>$date</b>")

            val finalEvent = index == blockedEvents.size - 1
            if (!finalEvent) {
                removedEventsHtml.append("<br/>")
            }
        }

        val errorCode = errorCodeStringFactory.get(HolderFlow.Refresh, listOf(AppErrorResult(HolderStep.GetCredentialsNetworkRequest, BlockedEventException())))
        infoFragmentUtil.presentAsBottomSheet(
            dashboardPageFragment.parentFragmentManager,
            InfoFragmentData.TitleDescription(
                title = dashboardPageFragment.getString(R.string.holder_invaliddetailsremoved_moreinfo_title),
                descriptionData = DescriptionData(
                    htmlTextString = dashboardPageFragment.getString(R.string.holder_invaliddetailsremoved_moreinfo_body, removedEventsHtml, errorCode),
                    htmlLinksEnabled = true
                )
            )
        )
    }

    private fun onOriginInfoClicked(
        dashboardPageFragment: DashboardPageFragment,
        item: DashboardItem.InfoItem.OriginInfoItem
    ) {
        when (item.greenCardType) {
            is GreenCardType.Domestic -> presentOriginInfoForDomesticQr(
                item.originType, dashboardPageFragment
            )
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
            is DashboardItem.InfoItem.DomesticVaccinationExpiredItem -> {
                dashboardPageFragment.dashboardViewModel.removeOrigin(infoItem.originEntity)
            }
            is DashboardItem.InfoItem.DomesticVaccinationAssessmentExpiredItem -> {
                dashboardPageFragment.dashboardViewModel.removeOrigin(infoItem.originEntity)
            }
            is DashboardItem.InfoItem.DisclosurePolicyItem -> {
                dashboardPageFragment.dashboardViewModel.dismissPolicyInfo(infoItem.disclosurePolicy)
            }
            is DashboardItem.InfoItem.BlockedEvents -> {
                dashboardPageFragment.dashboardViewModel.dismissBlockedEventsInfo()
            }
            else -> {
            }
        }
    }
}
