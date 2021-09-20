package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import nl.rijksoverheid.ctr.appconfig.usecases.ClockDeviationUseCase
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard

interface DashboardItemUtil {
    fun shouldShowHeaderItem(allGreenCards: List<GreenCard>): Boolean
    fun shouldShowClockDeviationItem(allGreenCards: List<GreenCard>): Boolean
    fun shouldShowPlaceholderItem(allGreenCards: List<GreenCard>): Boolean
    fun shouldAddQrButtonItem(allGreenCards: List<GreenCard>): Boolean
}

class DashboardItemUtilImpl(
    private val clockDeviationUseCase: ClockDeviationUseCase,
    private val greenCardUtil: GreenCardUtil
): DashboardItemUtil {
    override fun shouldShowHeaderItem(allGreenCards: List<GreenCard>) = allGreenCards.isNotEmpty() || !allGreenCards.all { greenCardUtil.isExpired(it) }
    override fun shouldShowClockDeviationItem(allGreenCards: List<GreenCard>) = clockDeviationUseCase.hasDeviation() && (allGreenCards.isNotEmpty() || !allGreenCards.all { greenCardUtil.isExpired(it) })
    override fun shouldShowPlaceholderItem(allGreenCards: List<GreenCard>) = allGreenCards.isEmpty() || allGreenCards.all { greenCardUtil.isExpired(it) }
    override fun shouldAddQrButtonItem(allGreenCards: List<GreenCard>): Boolean = allGreenCards.isEmpty()
}