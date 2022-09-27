/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard.util

import nl.rijksoverheid.ctr.design.ext.formatDateTime
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYear
import nl.rijksoverheid.ctr.design.fragments.info.DescriptionData
import nl.rijksoverheid.ctr.design.fragments.info.InfoFragmentData
import nl.rijksoverheid.ctr.design.utils.InfoFragmentUtil
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.dashboard.DashboardPageFragment
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEvent
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventNegativeTest
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventPositiveTest
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventRecovery
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccination
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccinationAssessment
import nl.rijksoverheid.ctr.holder.models.HolderFlow
import nl.rijksoverheid.ctr.holder.models.HolderStep
import nl.rijksoverheid.ctr.persistence.database.entities.BlockedEventEntity
import nl.rijksoverheid.ctr.shared.ext.capitalize
import nl.rijksoverheid.ctr.shared.factories.ErrorCodeStringFactory
import nl.rijksoverheid.ctr.shared.models.AppErrorResult
import nl.rijksoverheid.ctr.shared.models.BlockedEventException

interface ShowBlockedEventsBottomSheetUtil {
    fun show(dashboardPageFragment: DashboardPageFragment, blockedEvents: List<BlockedEventEntity>)
}

class ShowBlockedEventsBottomSheetUtilImpl(
    private val errorCodeStringFactory: ErrorCodeStringFactory,
    private val infoFragmentUtil: InfoFragmentUtil
) : ShowBlockedEventsBottomSheetUtil {

    override fun show(
        dashboardPageFragment: DashboardPageFragment,
        blockedEvents: List<BlockedEventEntity>
    ) {
        val context = dashboardPageFragment.requireContext()
        val removedEventsHtml = StringBuilder()
        blockedEvents.forEachIndexed { index, blockedEvent ->
            val remoteEventClass = RemoteEvent.getRemoteEventClassFromType(blockedEvent.type)

            val name = when (remoteEventClass) {
                RemoteEventVaccination::class.java -> context.getString(R.string.general_vaccination)
                    .capitalize()
                RemoteEventNegativeTest::class.java -> context.getString(R.string.general_negativeTest)
                    .capitalize()
                RemoteEventPositiveTest::class.java -> context.getString(R.string.general_positiveTest)
                    .capitalize()
                RemoteEventRecovery::class.java -> context.getString(R.string.general_recoverycertificate)
                RemoteEventVaccinationAssessment::class.java -> context.getString(R.string.general_visitorPass)
                else -> ""
            }
            val date = when (remoteEventClass) {
                RemoteEventVaccination::class.java -> "${context.getString(R.string.qr_card_vaccination_title_eu)} ${
                    blockedEvent.eventTime?.toLocalDate()?.formatDayMonthYear()
                }"
                RemoteEventNegativeTest::class.java -> "${context.getString(R.string.qr_card_test_title_eu)} ${
                    blockedEvent.eventTime?.formatDateTime(
                        context
                    )
                }"
                RemoteEventPositiveTest::class.java -> "${context.getString(R.string.qr_card_test_title_eu)} ${
                    blockedEvent.eventTime?.formatDateTime(
                        context
                    )
                }"
                RemoteEventRecovery::class.java -> "${context.getString(R.string.qr_card_recovery_title_eu)} ${
                    blockedEvent.eventTime?.toLocalDate()?.formatDayMonthYear()
                }"
                RemoteEventVaccinationAssessment::class.java -> "${context.getString(R.string.holder_event_vaccination_assessment_about_date)} ${
                    blockedEvent.eventTime?.formatDateTime(
                        context
                    )
                }"
                else -> ""
            }

            removedEventsHtml.append("<b>$name</b>")
            removedEventsHtml.append("<br/>")
            removedEventsHtml.append("<b>$date</b>")

            val finalEvent = index == blockedEvents.size - 1
            if (!finalEvent) {
                removedEventsHtml.append("<br/><br/>")
            }
        }

        val errorCode = errorCodeStringFactory.get(
            HolderFlow.Refresh, listOf(
                AppErrorResult(
                    HolderStep.GetCredentialsNetworkRequest, BlockedEventException()
                )
            )
        )

        infoFragmentUtil.presentAsBottomSheet(
            dashboardPageFragment.parentFragmentManager,
            InfoFragmentData.TitleDescription(
                title = context.getString(R.string.holder_invaliddetailsremoved_moreinfo_title),
                descriptionData = DescriptionData(
                    htmlTextString = context.getString(
                        R.string.holder_invaliddetailsremoved_moreinfo_body,
                        removedEventsHtml,
                        errorCode
                    ),
                    htmlLinksEnabled = true
                )
            )
        )
    }
}
