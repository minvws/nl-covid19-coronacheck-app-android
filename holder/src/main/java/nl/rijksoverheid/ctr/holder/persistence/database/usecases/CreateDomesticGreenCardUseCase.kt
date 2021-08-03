package nl.rijksoverheid.ctr.holder.persistence.database.usecases

import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteGreenCards
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import nl.rijksoverheid.ctr.shared.models.DomesticCredential
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

interface CreateDomesticGreenCardUseCase {
    suspend fun create(greenCard: RemoteGreenCards.DomesticGreenCard, domesticCredentials: List<DomesticCredential>)
}

class CreateDomesticGreenCardUseCaseImpl(
    private val holderDatabase: HolderDatabase,
): CreateDomesticGreenCardUseCase {
    override suspend fun create(greenCard: RemoteGreenCards.DomesticGreenCard, domesticCredentials: List<DomesticCredential>) {
        // Create green card
        val localDomesticGreenCardId = holderDatabase.greenCardDao().insert(
            GreenCardEntity(
                walletId = 1,
                type = GreenCardType.Domestic
            )
        )

        // Create origins
        greenCard.origins.forEach { remoteOrigin ->
            holderDatabase.originDao().insert(
                OriginEntity(
                    greenCardId = localDomesticGreenCardId,
                    type = remoteOrigin.type,
                    eventTime = remoteOrigin.eventTime,
                    expirationTime = remoteOrigin.expirationTime,
                    validFrom = remoteOrigin.validFrom
                )
            )
        }

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

