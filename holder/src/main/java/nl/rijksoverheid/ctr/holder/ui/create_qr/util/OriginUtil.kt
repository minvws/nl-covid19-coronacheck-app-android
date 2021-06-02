package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginEntity
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

interface OriginUtil {
    fun getValidOrigins(origins: List<OriginEntity>): List<OriginEntity>

    fun activeAt(euLaunchDate: String): OffsetDateTime
}

class OriginUtilImpl(private val clock: Clock): OriginUtil {
    override fun getValidOrigins(origins: List<OriginEntity>): List<OriginEntity> {
        return origins.filter {
            it.validFrom.isBefore(OffsetDateTime.now(clock))
                    && it.expirationTime.isAfter(OffsetDateTime.now(clock))
        }
    }

    override fun activeAt(euLaunchDate: String): OffsetDateTime {
        val timeFormatter = DateTimeFormatter.ISO_DATE_TIME
        val euLaunchDateInstant = Instant.from(timeFormatter.parse(euLaunchDate))
        return OffsetDateTime.ofInstant(euLaunchDateInstant, ZoneOffset.UTC)
    }
}