package nl.rijksoverheid.ctr.holder.persistence.database.usecases

import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteGreenCards
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

interface CreateEuGreenCardUseCase {
    suspend fun create(greenCard: RemoteGreenCards.EuGreenCard)
}

class CreateEuGreenCardUseCaseImpl(
    private val holderDatabase: HolderDatabase,
    private val mobileCoreWrapper: MobileCoreWrapper
): CreateEuGreenCardUseCase {
    override suspend fun create(greenCard: RemoteGreenCards.EuGreenCard) {
        // Create green card
        val localEuropeanGreenCardId = holderDatabase.greenCardDao().insert(
            GreenCardEntity(
                walletId = 1,
                type = GreenCardType.Eu
            )
        )

        // Create origins for european green card
        greenCard.origins.map { remoteOrigin ->
            holderDatabase.originDao().insert(
                OriginEntity(
                    greenCardId = localEuropeanGreenCardId,
                    type = remoteOrigin.type,
                    eventTime = remoteOrigin.eventTime,
                    expirationTime = remoteOrigin.expirationTime,
                    validFrom = remoteOrigin.validFrom
                )
            )
        }

        // Create credential
        val europeanCredential = mobileCoreWrapper.readEuropeanCredential(
            credential = greenCard.credential.toByteArray()
        )

        val entity = CredentialEntity(
            greenCardId = localEuropeanGreenCardId,
            data = greenCard.credential.toByteArray(),
            credentialVersion = europeanCredential.getInt("credentialVersion"),
            validFrom = OffsetDateTime.ofInstant(
                Instant.ofEpochSecond(europeanCredential.getLong("issuedAt")),
                ZoneOffset.UTC
            ),
            expirationTime = OffsetDateTime.ofInstant(
                Instant.ofEpochSecond(europeanCredential.getLong("expirationTime")),
                ZoneOffset.UTC
            )
        )

        holderDatabase.credentialDao().insert(entity)
    }
}

