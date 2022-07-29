package nl.rijksoverheid.ctr.persistence.database.usecases

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import nl.rijksoverheid.ctr.holder.your_events.models.RemoteGreenCards
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.CredentialEntity
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardEntity
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginEntity
import nl.rijksoverheid.ctr.persistence.database.entities.SecretKeyEntity
import nl.rijksoverheid.ctr.shared.models.DomesticCredential

interface CreateDomesticGreenCardUseCase {
    suspend fun create(greenCard: RemoteGreenCards.DomesticGreenCard, domesticCredentials: List<DomesticCredential>, secretKey: String)
}

class CreateDomesticGreenCardUseCaseImpl(
    private val holderDatabase: HolderDatabase
) : CreateDomesticGreenCardUseCase {
    override suspend fun create(greenCard: RemoteGreenCards.DomesticGreenCard, domesticCredentials: List<DomesticCredential>, secretKey: String) {
        // Create green card
        val localDomesticGreenCardId = holderDatabase.greenCardDao().insert(
            GreenCardEntity(
                walletId = 1,
                type = GreenCardType.Domestic
            )
        )

        // Save secret key
        holderDatabase
            .secretKeyDao()
            .insert(
                entity = SecretKeyEntity(
                    greenCardId = localDomesticGreenCardId.toInt(),
                    secretKey = secretKey
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
                    validFrom = remoteOrigin.validFrom,
                    doseNumber = remoteOrigin.doseNumber
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
                ).plusHours(domesticCredential.attributes.validForHours),
                category = domesticCredential.attributes.category
            )
        }

        holderDatabase.credentialDao().insertAll(entities)
    }
}
