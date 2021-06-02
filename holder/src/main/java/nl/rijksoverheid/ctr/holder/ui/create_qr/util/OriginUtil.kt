package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginEntity
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

interface OriginUtil {
    fun getValidOrigins(origins: List<OriginEntity>): List<OriginEntity>

    fun isActiveInEu(euLaunchDate: String): Boolean

    fun daysSinceActive(euLaunchDate: String): Long
}

class OriginUtilImpl(private val clock: Clock): OriginUtil {
    override fun getValidOrigins(origins: List<OriginEntity>): List<OriginEntity> {
        return origins.filter {
            it.validFrom.isBefore(OffsetDateTime.now(clock))
                    && it.expirationTime.isAfter(OffsetDateTime.now(clock))
        }
    }

    //euLaunchDate format: "2021-06-03T14:00:00+00:00"
    override fun isActiveInEu(euLaunchDate: String): Boolean {
        val timeFormatter = DateTimeFormatter.ISO_DATE_TIME
        val euLaunchDateInstant = Instant.from(timeFormatter.parse(euLaunchDate))
        return Instant.now(clock).isAfter(euLaunchDateInstant)
    }

    override fun daysSinceActive(euLaunchDate: String): Long {
        val timeFormatter = DateTimeFormatter.ISO_DATE_TIME
        val euLaunchDateInstant = Instant.from(timeFormatter.parse(euLaunchDate))
        return Instant.now(clock).until(euLaunchDateInstant, ChronoUnit.DAYS) + 1
    }
}