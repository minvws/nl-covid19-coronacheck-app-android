package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import java.time.Clock
import java.time.OffsetDateTime

interface GreenCardUtil {
    fun isExpired(greenCard: GreenCard): Boolean

    /**
     * Get the expiration date of the green card or of a specific origin of the green card
     *
     * @param[greenCard] green card to check expiration date
     * @param[type] Origin to get expiration from or if null latest expiration of all origins
     * @return Expiration time of origin type or latest expiration of all origins when not specified
     */
    fun getExpireDate(greenCard: GreenCard, type: OriginType? = null): OffsetDateTime

    fun getErrorCorrectionLevel(greenCardType: GreenCardType): ErrorCorrectionLevel

    fun isExpiring(renewalDays: Long, greenCard: GreenCard): Boolean

    fun hasNoActiveCredentials(greenCard: GreenCard): Boolean
}

class GreenCardUtilImpl(
    private val clock: Clock,
    private val credentialUtil: CredentialUtil): GreenCardUtil {

    override fun getExpireDate(greenCard: GreenCard, type: OriginType?): OffsetDateTime {
        return greenCard.origins
            .filter { if (type == null) true else type == it.type }
            .maxByOrNull { it.expirationTime }
            ?.expirationTime
            ?: OffsetDateTime.now(clock)
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

    override fun isExpiring(renewalDays: Long, greenCard: GreenCard): Boolean {
        val now = OffsetDateTime.now(clock)
        val expirationTime = getExpireDate(greenCard)
        return expirationTime.minusDays(renewalDays).isBefore(now)
    }

    override fun hasNoActiveCredentials(greenCard: GreenCard): Boolean {
        return credentialUtil.getActiveCredential(greenCard.credentialEntities) == null
    }
}