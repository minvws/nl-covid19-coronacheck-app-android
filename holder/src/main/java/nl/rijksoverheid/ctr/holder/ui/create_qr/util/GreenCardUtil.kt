package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import java.time.Clock
import java.time.OffsetDateTime

interface GreenCardUtil {
    fun isExpired(greenCard: GreenCard): Boolean
    fun getExpireDate(greenCard: GreenCard, type: OriginType? = null): OffsetDateTime
    fun getErrorCorrectionLevel(greenCardType: GreenCardType): ErrorCorrectionLevel
    fun isExpiring(renewalDays: Long, greenCard: GreenCard): Boolean
    fun hasNoActiveCredentials(greenCard: GreenCard): Boolean
    fun isExpiredDomesticVaccination(greenCard: GreenCard): Boolean
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

    override fun isExpiredDomesticVaccination(greenCard: GreenCard): Boolean {
        return greenCard.origins.any { it.type == OriginType.Vaccination } &&
                greenCard.greenCardEntity.type == GreenCardType.Domestic
    }
}