package nl.rijksoverheid.ctr.holder.fuzzy_matching

import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYear
import nl.rijksoverheid.ctr.holder.get_events.usecases.GetRemoteProtocolFromEventGroupUseCase
import nl.rijksoverheid.ctr.holder.your_events.utils.RemoteEventStringUtil
import nl.rijksoverheid.ctr.holder.your_events.utils.YourEventsFragmentUtil
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface SelectionDetailDataUtil {
    fun get(eventGroupEntity: EventGroupEntity): List<SelectionDetailData>
}

class SelectionDetailDataUtilImpl(
    private val getRemoteProtocolFromEventGroupUseCase: GetRemoteProtocolFromEventGroupUseCase,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase,
    private val yourEventsFragmentUtil: YourEventsFragmentUtil,
    private val remoteEventStringUtil: RemoteEventStringUtil
) : SelectionDetailDataUtil {
    override fun get(eventGroupEntity: EventGroupEntity): List<SelectionDetailData> {
        val remoteProtocol = getRemoteProtocolFromEventGroupUseCase.get(eventGroupEntity)
        val remoteEvents = remoteProtocol?.events ?: throw IllegalStateException("Invalid stored data")
        val configProviders = cachedAppConfigUseCase.getCachedAppConfig().providers

        val data = remoteEvents.map { event ->
            val eventDate = event.getDate()
            SelectionDetailData(
                type = remoteEventStringUtil.remoteEventTitle(event.javaClass),
                providerIdentifiers = listOf(remoteProtocol).map { yourEventsFragmentUtil.getProviderName(configProviders, it.providerIdentifier) },
                eventDate = eventDate?.toLocalDate()?.formatDayMonthYear() ?: ""
            )
        }

        return data
    }
}
