package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginEntity
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import java.time.Clock
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import nl.rijksoverheid.ctr.holder.ui.myoverview.items.MyOverviewGreenCardAdapterItem

interface OriginUtil {
    fun getOriginState(origins: List<OriginEntity>): List<OriginState>

    /**
     * If the origin subtitle should be shown in the [MyOverviewGreenCardAdapterItem]
     */
    fun presentSubtitle(greenCardType: GreenCardType, originState: OriginState): Boolean
}

class OriginUtilImpl(private val clock: Clock): OriginUtil {

    companion object {
        private const val PRESENT_SUBTITLE_WHEN_LESS_THEN_YEARS = 3
    }

    override fun getOriginState(origins: List<OriginEntity>): List<OriginState> {
        return origins.map { origin ->
            when {
                origin.expirationTime.isBefore(OffsetDateTime.now(clock)) -> {
                    OriginState.Expired(origin)
                }
                origin.validFrom.isAfter(OffsetDateTime.now(clock)) -> {
                    OriginState.Future(origin)
                }
                else -> {
                    OriginState.Valid(origin)
                }
            }
        }
    }

    override fun presentSubtitle(greenCardType: GreenCardType, originState: OriginState): Boolean {
        return (greenCardType == GreenCardType.Domestic && ChronoUnit.YEARS.between(OffsetDateTime.now(clock), originState.origin.validFrom) < PRESENT_SUBTITLE_WHEN_LESS_THEN_YEARS) || greenCardType == GreenCardType.Eu
    }
}

sealed class OriginState(open val origin: OriginEntity) {
    data class Valid(override val origin: OriginEntity) : OriginState(origin)
    data class Future(override val origin: OriginEntity) : OriginState(origin)
    data class Expired(override val origin: OriginEntity) : OriginState(origin)
}