package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import nl.rijksoverheid.ctr.appconfig.usecases.ClockDeviationUseCase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem

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
}

class DashboardItemUtilImpl(
    private val clockDeviationUseCase: ClockDeviationUseCase,
    private val greenCardUtil: GreenCardUtil
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
}