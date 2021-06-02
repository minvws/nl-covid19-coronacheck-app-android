package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginEntity
import java.time.*

interface OriginUtil {
    fun getValidOrigins(origins: List<OriginEntity>): List<OriginEntity>

    fun isActiveInEu(euLaunchDate: String): Boolean
}

class OriginUtilImpl(private val clock: Clock): OriginUtil {
    override fun getValidOrigins(origins: List<OriginEntity>): List<OriginEntity> {
        return origins.filter {
            it.validFrom.isBefore(OffsetDateTime.now(clock))
                    && it.expirationTime.isAfter(OffsetDateTime.now(clock))
        }
    }

    //euLaunchDate format: "2021-07-01"
    override fun isActiveInEu(euLaunchDate: String): Boolean {
        return LocalDate.now(clock).isAfter(LocalDate.parse(euLaunchDate))
    }
}