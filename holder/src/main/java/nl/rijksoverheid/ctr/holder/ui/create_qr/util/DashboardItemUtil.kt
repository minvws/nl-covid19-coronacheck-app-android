package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import nl.rijksoverheid.ctr.appconfig.usecases.AppConfigFreshnessUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.ClockDeviationUseCase
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem

interface DashboardItemUtil {
    fun getHeaderItemText(greenCardType: GreenCardType, allGreenCards: List<GreenCard>): Int
    fun shouldShowClockDeviationItem(allGreenCards: List<GreenCard>): Boolean
    fun shouldShowPlaceholderItem(allGreenCards: List<GreenCard>): Boolean
    fun shouldAddQrButtonItem(allGreenCards: List<GreenCard>): Boolean

    /**
     * Multiple EU vaccination green card items will be combined into 1.
     *
     * @param[items] Items list containing possible multiple vaccination items to combine.
     * @return Items list with vaccination green card items combined into 1.
     */
    fun combineEuVaccinationItems(items: List<DashboardItem>): List<DashboardItem>

    suspend fun shouldAddSyncGreenCardsItem(
        allEventGroupEntities: List<EventGroupEntity>,
        allGreenCards: List<GreenCard>): Boolean
    fun shouldAddGreenCardsSyncedItem(allGreenCards: List<GreenCard>): Boolean

    fun shouldShowExtendDomesticRecoveryItem(): Boolean
    fun shouldShowRecoverDomesticRecoveryItem(): Boolean
    fun shouldShowExtendedDomesticRecoveryItem(): Boolean
    fun shouldShowRecoveredDomesticRecoveryItem(): Boolean
    fun shouldShowConfigFreshnessWarning(): Boolean
    fun getConfigFreshnessMaxValidity() : Long
}

class DashboardItemUtilImpl(
    private val clockDeviationUseCase: ClockDeviationUseCase,
    private val greenCardUtil: GreenCardUtil,
    private val persistenceManager: PersistenceManager,
    private val eventGroupEntityUtil: EventGroupEntityUtil,
    private val appConfigFreshnessUseCase: AppConfigFreshnessUseCase
) : DashboardItemUtil {

    override fun getHeaderItemText(greenCardType: GreenCardType, allGreenCards: List<GreenCard>): Int {
        val hasGreenCards = allGreenCards.isNotEmpty() && !allGreenCards.all { greenCardUtil.isExpired(it) }
        return when (greenCardType) {
            is GreenCardType.Domestic -> {
                if (hasGreenCards) {
                    R.string.my_overview_description
                } else {
                    R.string.my_overview_qr_placeholder_description
                }
            }
            is GreenCardType.Eu -> {
                if (hasGreenCards) {
                    R.string.my_overview_description_eu
                } else {
                    R.string.my_overview_qr_placeholder_description_eu
                }
            }
        }
    }

    override fun shouldShowClockDeviationItem(allGreenCards: List<GreenCard>) =
        clockDeviationUseCase.hasDeviation() && (allGreenCards.isNotEmpty() ||
                !allGreenCards.all { greenCardUtil.isExpired(it) })

    override fun shouldShowPlaceholderItem(allGreenCards: List<GreenCard>) =
        allGreenCards.isEmpty() || allGreenCards.all { greenCardUtil.isExpired(it) }

    override fun shouldAddQrButtonItem(allGreenCards: List<GreenCard>): Boolean =
        allGreenCards.isEmpty()

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

    override suspend fun shouldAddSyncGreenCardsItem(
        allEventGroupEntities: List<EventGroupEntity>,
        allGreenCards: List<GreenCard>): Boolean {
        val amountOfVaccinationEvents = eventGroupEntityUtil.amountOfVaccinationEvents(allEventGroupEntities)
        return if (amountOfVaccinationEvents in 0..1) {
            // If we only have a single vaccination event (e.g. hkvi) we'll never get more cards
            false
        } else {
            // there are more than 1 vaccination events. If this
            // isn't reflected by
            // our current set of greencards, show the banner to offer
            // people an upgrade.
            val euVaccinationGreenCards = allGreenCards.filter { it.greenCardEntity.type is GreenCardType.Eu }.filter { it.origins.any { origin -> origin.type is OriginType.Vaccination } }
            euVaccinationGreenCards.size == 1
        }
    }

    override fun shouldAddGreenCardsSyncedItem(allGreenCards: List<GreenCard>): Boolean {
        val euVaccinationGreenCards = allGreenCards.filter { it.greenCardEntity.type is GreenCardType.Eu }.filter { it.origins.any { origin -> origin.type is OriginType.Vaccination } }

        // Only show banner if;
        // - there are more than one european vaccinations
        // - the banner has not been dismissed
        return (euVaccinationGreenCards.size > 1 && !persistenceManager.hasDismissedSyncedGreenCardsItem())
    }

    override fun shouldShowExtendDomesticRecoveryItem(): Boolean {
        return persistenceManager.getShowExtendDomesticRecoveryInfoCard()
    }

    override fun shouldShowRecoverDomesticRecoveryItem(): Boolean {
        return persistenceManager.getShowRecoverDomesticRecoveryInfoCard()
    }

    override fun shouldShowExtendedDomesticRecoveryItem(): Boolean {
        return !persistenceManager.getHasDismissedExtendedDomesticRecoveryInfoCard()
    }

    override fun shouldShowRecoveredDomesticRecoveryItem(): Boolean {
        return !persistenceManager.getHasDismissedRecoveredDomesticRecoveryInfoCard()
    }

    override fun shouldShowConfigFreshnessWarning(): Boolean {
        // return true if config is older than 10 days && less than 28 days
       return appConfigFreshnessUseCase.shouldShowConfigFreshnessWarning()
    }

    override fun getConfigFreshnessMaxValidity(): Long {
        return appConfigFreshnessUseCase.getAppConfigMaxValidityTimestamp()
    }
}