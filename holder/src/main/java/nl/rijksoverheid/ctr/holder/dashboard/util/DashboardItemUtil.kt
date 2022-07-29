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
import nl.rijksoverheid.ctr.persistence.PersistenceManager
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.shared.BuildConfigUseCase
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy

interface DashboardItemUtil {
    fun shouldShowClockDeviationItem(emptyState: Boolean, allGreenCards: List<GreenCard>): Boolean
    fun shouldShowPlaceholderItem(emptyState: Boolean): Boolean
    fun shouldAddQrButtonItem(emptyState: Boolean): Boolean
    fun isAppUpdateAvailable(): Boolean

    /**
     * Multiple EU vaccination green card items will be combined into 1.
     *
     * @param[items] Items list containing possible multiple vaccination items to combine.
     * @return Items list with vaccination green card items combined into 1.
     */
    fun combineEuVaccinationItems(items: List<DashboardItem>): List<DashboardItem>

    fun shouldShowConfigFreshnessWarning(): Boolean
    fun getConfigFreshnessMaxValidity(): Long
    fun shouldShowMissingDutchVaccinationItem(
        domesticGreenCards: List<GreenCard>,
        euGreenCards: List<GreenCard>
    ): Boolean
    fun shouldShowVisitorPassIncompleteItem(
        events: List<EventGroupEntity>,
        domesticGreenCards: List<GreenCard>
    ): Boolean
    fun shouldShowOriginInfoItem(
        disclosurePolicy: DisclosurePolicy,
        greenCards: List<GreenCard>,
        greenCardType: GreenCardType,
        originType: OriginType
    ): Boolean
    fun shouldShowAddQrCardItem(
        hasVisitorPassIncompleteItem: Boolean,
        emptyState: Boolean
    ): Boolean
    fun shouldShowPolicyInfoItem(
        disclosurePolicy: DisclosurePolicy,
        tabType: GreenCardType
    ): Boolean
}

class DashboardItemUtilImpl(
    private val clockDeviationUseCase: ClockDeviationUseCase,
    private val persistenceManager: PersistenceManager,
    private val appConfigFreshnessUseCase: AppConfigFreshnessUseCase,
    private val appConfigUseCase: HolderCachedAppConfigUseCase,
    private val buildConfigUseCase: BuildConfigUseCase,
    private val greenCardUtil: GreenCardUtil
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

    override fun shouldShowMissingDutchVaccinationItem(
        domesticGreenCards: List<GreenCard>,
        euGreenCards: List<GreenCard>
    ): Boolean {
        // if a user has a european vaccination certificate but not dutch one,
        // we inform him that he can get a dutch one by either retrieving a
        // second vaccination result or a positive test result
        return domesticGreenCards.none { it.origins.any { it.type == OriginType.Vaccination } } &&
                euGreenCards.any { it.origins.any { it.type == OriginType.Vaccination } }
    }

    override fun shouldShowVisitorPassIncompleteItem(
        events: List<EventGroupEntity>,
        domesticGreenCards: List<GreenCard>
    ): Boolean {
        val hasVaccinationAssessmentEvent = events.map { it.type }.contains(OriginType.VaccinationAssessment)
        val hasVaccinationAssessmentOrigin = domesticGreenCards.map { it.origins.map { origin -> origin.type } }.flatten().contains(OriginType.VaccinationAssessment)
        return hasVaccinationAssessmentEvent && !hasVaccinationAssessmentOrigin
    }

    override fun shouldShowOriginInfoItem(
        disclosurePolicy: DisclosurePolicy,
        greenCards: List<GreenCard>,
        greenCardType: GreenCardType,
        originInfoTypeOrigin: OriginType
    ): Boolean {
        return when (disclosurePolicy) {
            is DisclosurePolicy.ZeroG -> {
                false
            }
            else -> {
                val hasVaccinationAssessmentOrigin = greenCardUtil.hasOrigin(
                    greenCards = greenCards,
                    originType = OriginType.VaccinationAssessment
                )

                // We do not show the origin info item for a domestic test if there is a vaccination assessment green card active (this causes some confusion in the UI)
                !(hasVaccinationAssessmentOrigin && originInfoTypeOrigin == OriginType.Test && greenCardType == GreenCardType.Domestic)
            }
        }
    }

    override fun shouldShowAddQrCardItem(
        hasVisitorPassIncompleteItem: Boolean,
        emptyState: Boolean
    ) = !emptyState && !hasVisitorPassIncompleteItem

    override fun shouldShowPolicyInfoItem(
        disclosurePolicy: DisclosurePolicy,
        tabType: GreenCardType
    ): Boolean {
        return if (persistenceManager.getPolicyBannerDismissed() != disclosurePolicy) {
            when (tabType) {
                is GreenCardType.Domestic -> {
                    disclosurePolicy !is DisclosurePolicy.ZeroG
                }
                is GreenCardType.Eu -> {
                    disclosurePolicy is DisclosurePolicy.ZeroG
                }
            }
        } else {
            false
        }
    }
}
