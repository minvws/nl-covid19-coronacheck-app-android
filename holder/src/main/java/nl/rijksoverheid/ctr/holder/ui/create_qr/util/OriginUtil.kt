package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginEntity
import java.time.*

interface OriginUtil {

    fun getOriginState(origins: List<OriginEntity>): List<OriginState>
    fun isActiveInEu(euLaunchDate: String): Boolean
}

class OriginUtilImpl(private val clock: Clock): OriginUtil {

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

    //euLaunchDate format: "2021-07-01"
    override fun isActiveInEu(euLaunchDate: String): Boolean {
        return LocalDate.now(clock).isAfter(LocalDate.parse(euLaunchDate))
    }
}

sealed class OriginState(open val origin: OriginEntity) {
    data class Valid(override val origin: OriginEntity) : OriginState(origin)
    data class Future(override val origin: OriginEntity) : OriginState(origin)
    data class Expired(override val origin: OriginEntity) : OriginState(origin)
}