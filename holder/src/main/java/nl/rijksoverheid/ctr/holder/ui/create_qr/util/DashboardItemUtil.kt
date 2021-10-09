package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import nl.rijksoverheid.ctr.appconfig.usecases.ClockDeviationUseCase
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper

interface DashboardItemUtil {
    fun shouldShowHeaderItem(allGreenCards: List<GreenCard>): Boolean
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

    fun shouldAddSyncGreenCardsItem(allGreenCards: List<GreenCard>): Boolean
    fun shouldAddGreenCardsSyncedItem(allGreenCards: List<GreenCard>): Boolean
}

class DashboardItemUtilImpl(
    private val readEuropeanCredentialUtil: ReadEuropeanCredentialUtil,
    private val mobileCoreWrapper: MobileCoreWrapper,
    private val clockDeviationUseCase: ClockDeviationUseCase,
    private val greenCardUtil: GreenCardUtil,
    private val persistenceManager: PersistenceManager
) : DashboardItemUtil {

    override fun shouldShowHeaderItem(allGreenCards: List<GreenCard>) =
        allGreenCards.isNotEmpty() || !allGreenCards.all { greenCardUtil.isExpired(it) }

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

    override fun shouldAddSyncGreenCardsItem(allGreenCards: List<GreenCard>): Boolean {
        val euVaccinationGreenCards = allGreenCards.filter { it.greenCardEntity.type is GreenCardType.Eu }.filter { it.origins.any { origin -> origin.type is OriginType.Vaccination } }

        // We only show the banner to refresh the green cards if;
        // - there is only one european vaccination
        // - that european vaccination has dosis "2"
        // this means you can update to our "multiple dcc" feature which will give you 2 green cards (1/2 and 2/2)
        val hasSecondDose = if (euVaccinationGreenCards.size == 1) {
            val credential = euVaccinationGreenCards.first().credentialEntities.firstOrNull()
            if (credential == null) {
                false
            } else {
                val readEuropeanCredential = mobileCoreWrapper.readEuropeanCredential(credential.data)
                val dose = readEuropeanCredentialUtil.getDose(readEuropeanCredential)
                dose == "2"
            }
        } else false

        return hasSecondDose && persistenceManager.showSyncGreenCardsItem()
    }

    override fun shouldAddGreenCardsSyncedItem(allGreenCards: List<GreenCard>): Boolean {
        val euVaccinationGreenCards = allGreenCards.filter { it.greenCardEntity.type is GreenCardType.Eu }.filter { it.origins.any { origin -> origin.type is OriginType.Vaccination } }

        // Only show banner if;
        // - there are more than one european vaccinations
        // - the banner has not been dismissed
        return (euVaccinationGreenCards.size > 1 && !persistenceManager.hasDismissedSyncedGreenCardsItem())
    }
}