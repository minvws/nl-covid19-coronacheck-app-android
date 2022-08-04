/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard.util

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.time.Clock
import java.time.OffsetDateTime
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy

interface GreenCardUtil {
    suspend fun getAllGreenCards(): List<GreenCard>

    fun hasOrigin(greenCards: List<GreenCard>, originType: OriginType): Boolean

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

    /**
     * When in 1G or 1G/3G [DisclosurePolicy] mode, this returns true if we are dealing with
     * a green card that was splitted to represent a single green card that has a test origin
     * @param greenCard The greencard to check
     */
    fun isDomesticTestGreenCard(greenCard: GreenCard): Boolean

    fun isForeignDcc(greenCard: GreenCard): Boolean
}

class GreenCardUtilImpl(
    private val holderDatabase: HolderDatabase,
    private val clock: Clock,
    private val credentialUtil: CredentialUtil,
    private val mobileCoreWrapper: MobileCoreWrapper
) : GreenCardUtil {

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

    override suspend fun getAllGreenCards(): List<GreenCard> {
        return holderDatabase
            .greenCardDao()
            .getAll()
            .filter { it.origins.isNotEmpty() }
    }

    override fun hasOrigin(greenCards: List<GreenCard>, originType: OriginType): Boolean {
        return greenCards.map { it.origins }.flatten().map { it.type }.any { it == originType }
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
        return credentialUtil.getActiveCredential(greenCard.greenCardEntity.type, greenCard.credentialEntities) == null
    }

    override fun isDomesticTestGreenCard(greenCard: GreenCard): Boolean {
        return greenCard.greenCardEntity.type == GreenCardType.Domestic &&
                greenCard.origins.size == 1 && hasOrigin(listOf(greenCard), OriginType.Test)
    }

    override fun isForeignDcc(greenCard: GreenCard): Boolean {
        return when (greenCard.greenCardEntity.type) {
            is GreenCardType.Domestic -> {
                false
            }
            is GreenCardType.Eu -> {
                val activeCredential = credentialUtil.getActiveCredential(greenCard.greenCardEntity.type, greenCard.credentialEntities)
                activeCredential?.let {
                    mobileCoreWrapper.isForeignDcc(activeCredential.data)
                } ?: false
            }
        }
    }
}
