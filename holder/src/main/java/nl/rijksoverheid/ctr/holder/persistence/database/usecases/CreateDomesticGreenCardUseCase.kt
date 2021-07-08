package nl.rijksoverheid.ctr.holder.persistence.database.usecases

import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteGreenCards
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

interface CreateDomesticGreenCardUseCase {
    suspend fun create(greenCard: RemoteGreenCards.DomesticGreenCard)
}

class CreateDomesticGreenCardUseCaseImpl(
    private val holderDatabase: HolderDatabase,
    private val mobileCoreWrapper: MobileCoreWrapper
): CreateDomesticGreenCardUseCase {
    override suspend fun create(greenCard: RemoteGreenCards.DomesticGreenCard) {
        // Create green card
        val localDomesticGreenCardId = holderDatabase.greenCardDao().insert(
            GreenCardEntity(
                walletId = 1,
                type = GreenCardType.Domestic
            )
        )

        // Create origins
        greenCard.origins.forEach { remoteOrigin ->
            val type = when (remoteOrigin.type) {
                OriginType.TYPE_VACCINATION -> OriginType.Vaccination
                OriginType.TYPE_RECOVERY -> OriginType.Recovery
                OriginType.TYPE_TEST -> OriginType.Test
                else -> throw IllegalStateException("Type not known")
            }
            holderDatabase.originDao().insert(
                OriginEntity(
                    greenCardId = localDomesticGreenCardId,
                    type = type,
                    eventTime = remoteOrigin.eventTime,
                    expirationTime = remoteOrigin.expirationTime,
                    validFrom = remoteOrigin.validFrom
                )
            )
        }

        // Create credentials
        val domesticCredentials = mobileCoreWrapper.createDomesticCredentials(
            createCredentials = greenCard.createCredentialMessages
        )

        val entities = domesticCredentials.map { domesticCredential ->
            CredentialEntity(
                greenCardId = localDomesticGreenCardId,
                data = domesticCredential.credential.toString().replace("\\/", "/").toByteArray(),
                credentialVersion = domesticCredential.attributes.credentialVersion,
                validFrom = OffsetDateTime.ofInstant(
                    Instant.ofEpochSecond(domesticCredential.attributes.validFrom),
                    ZoneOffset.UTC
                ),
                expirationTime = OffsetDateTime.ofInstant(
                    Instant.ofEpochSecond(domesticCredential.attributes.validFrom),
                    ZoneOffset.UTC
                ).plusHours(domesticCredential.attributes.validForHours)
            )
        }

        holderDatabase.credentialDao().insertAll(entities)
    }
}

