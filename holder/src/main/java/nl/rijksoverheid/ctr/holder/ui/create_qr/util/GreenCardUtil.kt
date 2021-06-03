package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import java.time.Clock
import java.time.OffsetDateTime

interface GreenCardUtil {
    fun isExpired(greenCard: GreenCard): Boolean
    fun getExpireDate(greenCard: GreenCard): OffsetDateTime
    fun getErrorCorrectionLevel(greenCardType: GreenCardType): ErrorCorrectionLevel
}

class GreenCardUtilImpl(private val clock: Clock): GreenCardUtil {

    override fun getExpireDate(greenCard: GreenCard): OffsetDateTime {
        return greenCard.origins.maxByOrNull { it.expirationTime }?.expirationTime ?: OffsetDateTime.now(clock)
    }

    override fun getErrorCorrectionLevel(greenCardType: GreenCardType): ErrorCorrectionLevel {
        return when (greenCardType) {
            is GreenCardType.Domestic -> ErrorCorrectionLevel.M
            is GreenCardType.Eu -> ErrorCorrectionLevel.Q
        }
    }

    override fun isExpired(greenCard: GreenCard): Boolean {
        return OffsetDateTime.now(clock) >= getExpireDate(greenCard)
    }
}