package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginEntity
import java.time.Clock
import java.time.OffsetDateTime
import java.time.ZonedDateTime

interface OriginUtil {
    fun getValidOrigins(origins: List<OriginEntity>): List<OriginEntity>

    fun hasLaunchedInEu(euLaunchDate: String): Boolean
}

class OriginUtilImpl(private val clock: Clock): OriginUtil {
    override fun getValidOrigins(origins: List<OriginEntity>): List<OriginEntity> {
        return origins.filter {
            it.validFrom.isBefore(OffsetDateTime.now(clock))
                    && it.expirationTime.isAfter(OffsetDateTime.now(clock))
        }
    }

    //euLaunchDate format: "2021-07-01"
    override fun hasLaunchedInEu(euLaunchDate: String): Boolean {
        return ZonedDateTime.parse(euLaunchDate).withFixedOffsetZone().isAfter(
            ZonedDateTime.now(clock))
    }
}