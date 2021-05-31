package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginEntity
import java.time.Clock
import java.time.OffsetDateTime

interface OriginUtil {
    fun getValidOrigins(origins: List<OriginEntity>): List<OriginEntity>
}

class OriginUtilImpl(private val clock: Clock): OriginUtil {
    override fun getValidOrigins(origins: List<OriginEntity>): List<OriginEntity> {
        return origins.filter {
            it.validFrom.isBefore(OffsetDateTime.now(clock))
                    && it.expirationTime.isAfter(OffsetDateTime.now(clock))
        }
    }
}