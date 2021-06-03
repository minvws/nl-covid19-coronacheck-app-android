package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import java.time.Clock
import java.time.OffsetDateTime

interface GreenCardUtil {
    fun isExpired(greenCard: GreenCard): Boolean
    fun getExpireDate(greencard: GreenCard): OffsetDateTime
}

class GreenCardUtilImpl(private val clock: Clock): GreenCardUtil {

    override fun getExpireDate(greenCard: GreenCard): OffsetDateTime {
        return greenCard.origins.maxByOrNull { it.expirationTime }?.expirationTime ?: OffsetDateTime.now(clock)
    }

    override fun isExpired(greenCard: GreenCard): Boolean {
        return OffsetDateTime.now(clock) >= getExpireDate(greenCard)
    }
}