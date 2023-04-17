/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard.util

import nl.rijksoverheid.ctr.appconfig.usecases.AppConfigFreshnessUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.ClockDeviationUseCase
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardItem
import nl.rijksoverheid.ctr.persistence.HolderCachedAppConfigUseCase
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.persistence.database.entities.RemovedEventReason
import nl.rijksoverheid.ctr.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.shared.BuildConfigUseCase

interface DashboardItemUtil {
    fun shouldShowClockDeviationItem(emptyState: Boolean, allGreenCards: List<GreenCard>): Boolean
    fun shouldShowPlaceholderItem(emptyState: Boolean): Boolean
    fun shouldAddQrButtonItem(emptyState: Boolean): Boolean
    fun isAppUpdateAvailable(): Boolean
    suspend fun shouldShowBlockedEventsItem(): Boolean
    suspend fun shouldShowFuzzyMatchedEventsItem(): Boolean

    /**
     * Multiple EU vaccination green card items will be combined into 1.
     *
     * @param[items] Items list containing possible multiple vaccination items to combine.
     * @return Items list with vaccination green card items combined into 1.
     */
    fun combineEuVaccinationItems(items: List<DashboardItem>): List<DashboardItem>

    fun shouldShowConfigFreshnessWarning(): Boolean
    fun getConfigFreshnessMaxValidity(): Long

    fun shouldShowOriginInfoItem(
        greenCards: List<GreenCard>,
        greenCardType: GreenCardType,
        originType: OriginType
    ): Boolean

    fun shouldShowAddQrCardItem(
        hasVisitorPassIncompleteItem: Boolean,
        emptyState: Boolean
    ): Boolean
}

class DashboardItemUtilImpl(
    private val clockDeviationUseCase: ClockDeviationUseCase,
    private val appConfigFreshnessUseCase: AppConfigFreshnessUseCase,
    private val appConfigUseCase: HolderCachedAppConfigUseCase,
    private val buildConfigUseCase: BuildConfigUseCase,
    private val holderDatabase: HolderDatabase
) : DashboardItemUtil {

    override fun shouldShowClockDeviationItem(emptyState: Boolean, allGreenCards: List<GreenCard>) =
        clockDeviationUseCase.hasDeviation() && (!emptyState)

    override fun shouldShowPlaceholderItem(
        emptyState: Boolean
    ) = emptyState

    override fun shouldAddQrButtonItem(emptyState: Boolean): Boolean = emptyState

    override fun isAppUpdateAvailable(): Boolean {
        return buildConfigUseCase.getVersionCode() < appConfigUseCase.getCachedAppConfig().recommendedVersion
    }

    override suspend fun shouldShowBlockedEventsItem(): Boolean {
        return holderDatabase.removedEventDao().getAll(reason = RemovedEventReason.Blocked)
            .isNotEmpty()
    }

    override suspend fun shouldShowFuzzyMatchedEventsItem(): Boolean {
        val storedEvents = holderDatabase.eventGroupDao().getAll()
        // if user has removed his events from the menu, there is no point on showing the banner
        if (storedEvents.isEmpty()) {
            holderDatabase.removedEventDao().deleteAll(RemovedEventReason.FuzzyMatched)
            return false
        }
        return holderDatabase.removedEventDao().getAll(reason = RemovedEventReason.FuzzyMatched)
            .isNotEmpty()
    }

    override fun combineEuVaccinationItems(items: List<DashboardItem>): List<DashboardItem> {
        return items
            .groupBy { it::class }
            .map { itemTypeToItem ->
                if (itemTypeToItem.value.first() !is DashboardItem.CardsItem) {
                    itemTypeToItem.value
                } else {
                    itemTypeToItem.value
                        .groupBy { (it as DashboardItem.CardsItem).cards.first().greenCard.origins.first().type }
                        .map {
                            if (it.key == OriginType.Vaccination) {
                                listOf(
                                    DashboardItem.CardsItem(it.value.map { greenCardsItem ->
                                        (greenCardsItem as DashboardItem.CardsItem).cards
                                    }.flatten())
                                )
                            } else it.value
                        }.flatten()
                }
            }.flatten()
    }

    override fun shouldShowConfigFreshnessWarning(): Boolean {
        // return true if config is older than 10 days && less than 28 days
        return appConfigFreshnessUseCase.shouldShowConfigFreshnessWarning()
    }

    override fun getConfigFreshnessMaxValidity(): Long {
        return appConfigFreshnessUseCase.getAppConfigMaxValidityTimestamp()
    }

    override fun shouldShowOriginInfoItem(
        greenCards: List<GreenCard>,
        greenCardType: GreenCardType,
        originType: OriginType
    ): Boolean {
        return false
    }

    override fun shouldShowAddQrCardItem(
        hasVisitorPassIncompleteItem: Boolean,
        emptyState: Boolean
    ) = !emptyState && !hasVisitorPassIncompleteItem
}
