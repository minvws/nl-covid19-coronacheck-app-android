/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard.util

import android.content.Context
import androidx.core.text.HtmlCompat
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
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
import nl.rijksoverheid.ctr.holder.get_events.usecases.GetRemoteProtocolFromEventGroupUseCase
import nl.rijksoverheid.ctr.holder.models.HolderFlow
import nl.rijksoverheid.ctr.holder.models.HolderStep
import nl.rijksoverheid.ctr.holder.your_events.utils.RemoteEventStringUtil
import nl.rijksoverheid.ctr.holder.your_events.utils.YourEventsFragmentUtil
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.persistence.database.entities.RemovedEventEntity
import nl.rijksoverheid.ctr.shared.factories.ErrorCodeStringFactory
import nl.rijksoverheid.ctr.shared.models.AppErrorResult
import nl.rijksoverheid.ctr.shared.models.BlockedEventException

interface RemovedEventsBottomSheetUtil {
    fun presentBlockedEvents(dashboardPageFragment: DashboardPageFragment, blockedEvents: List<RemovedEventEntity>)
    fun presentRemovedEvents(dashboardPageFragment: DashboardPageFragment, storedEvent: EventGroupEntity, removedEvents: List<RemovedEventEntity>)
}

class RemovedEventsBottomSheetUtilImpl(
    private val errorCodeStringFactory: ErrorCodeStringFactory,
    private val remoteEventStringUtil: RemoteEventStringUtil,
    private val getRemoteProtocolFromEventGroupUseCase: GetRemoteProtocolFromEventGroupUseCase,
    private val yourEventsFragmentUtil: YourEventsFragmentUtil,
    private val infoFragmentUtil: InfoFragmentUtil,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase
) : RemovedEventsBottomSheetUtil {

    private fun formattedEvents(context: Context, events: List<RemovedEventEntity>): String {
        val removedEventsHtml = StringBuilder()
        events.forEachIndexed { index, blockedEvent ->
            val remoteEventClass = RemoteEvent.getRemoteEventClassFromType(blockedEvent.type)

            val title = remoteEventStringUtil.remoteEventTitle(remoteEventClass)
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
                else -> ""
            }

            removedEventsHtml.append("<b>$title</b>")
            removedEventsHtml.append("<br/>")
            removedEventsHtml.append("<b>$date</b>")

            val finalEvent = index == events.size - 1
            if (!finalEvent) {
                removedEventsHtml.append("<br/><br/>")
            }
        }

        return removedEventsHtml.toString()
    }

    override fun presentBlockedEvents(
        dashboardPageFragment: DashboardPageFragment,
        blockedEvents: List<RemovedEventEntity>
    ) {
        val context = dashboardPageFragment.requireContext()
        val removedEventsHtml = formattedEvents(context, blockedEvents)

        val errorCode = errorCodeStringFactory.get(
            HolderFlow.Refresh, listOf(
                AppErrorResult(
                    HolderStep.GetCredentialsNetworkRequest, BlockedEventException()
                )
            )
        )

        val contactInformation = cachedAppConfigUseCase.getCachedAppConfig().contactInfo
        infoFragmentUtil.presentAsBottomSheet(
            dashboardPageFragment.parentFragmentManager,
            InfoFragmentData.TitleDescription(
                title = context.getString(R.string.holder_invaliddetailsremoved_moreinfo_title),
                descriptionData = DescriptionData(
                    htmlTextString = context.getString(
                        R.string.holder_invaliddetailsremoved_moreinfo_body,
                        removedEventsHtml,
                        contactInformation.phoneNumber,
                        contactInformation.phoneNumber,
                        errorCode
                    ),
                    htmlLinksEnabled = true
                )
            )
        )
    }

    override fun presentRemovedEvents(
        dashboardPageFragment: DashboardPageFragment,
        storedEvent: EventGroupEntity,
        removedEvents: List<RemovedEventEntity>
    ) {
        val context = dashboardPageFragment.requireContext()
        val removedEventsHtml = formattedEvents(context, removedEvents)

        val name = yourEventsFragmentUtil.getFullName(getRemoteProtocolFromEventGroupUseCase.get(storedEvent)?.holder)

        infoFragmentUtil.presentAsBottomSheet(
            dashboardPageFragment.parentFragmentManager,
            InfoFragmentData.TitleDescription(
                title = context.getString(R.string.holder_identityRemoved_moreinfo_title),
                descriptionData = DescriptionData(
                    htmlTextString = context.getString(
                        R.string.holder_identityRemoved_moreinfo_body,
                        HtmlCompat.fromHtml(name, HtmlCompat.FROM_HTML_MODE_LEGACY),
                        removedEventsHtml
                    ),
                    htmlLinksEnabled = true
                )
            )
        )
    }
}
