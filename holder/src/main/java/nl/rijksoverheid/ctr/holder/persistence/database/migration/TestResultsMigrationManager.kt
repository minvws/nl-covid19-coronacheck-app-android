package nl.rijksoverheid.ctr.holder.persistence.database.migration

import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.*
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

interface TestResultsMigrationManager {
    suspend fun migrateTestResults()
}

class TestResultsMigrationManagerImpl(
    private val persistenceManager: PersistenceManager,
    private val mobileCoreWrapper: MobileCoreWrapper,
    private val holderDatabase: HolderDatabase,
) : TestResultsMigrationManager {
    override suspend fun migrateTestResults() {

        val existingCredentials = persistenceManager.getCredentials()
        if (existingCredentials != null) {
            try {
                val localDomesticGreenCardId = holderDatabase.greenCardDao().insert(
                    GreenCardEntity(
                        walletId = 1,
                        type = GreenCardType.Domestic
                    )
                )

                val legacyCredentials =
                    mobileCoreWrapper.readCredentialLegacy(existingCredentials.toByteArray())

                val validFrom = OffsetDateTime.of(
                    LocalDateTime.ofEpochSecond(
                        legacyCredentials.attributes.validFrom,
                        0,
                        ZoneOffset.UTC
                    ), ZoneOffset.UTC
                )
                val originType = OriginType.Test
                holderDatabase.originDao().insert(
                    OriginEntity(
                        greenCardId = localDomesticGreenCardId,
                        type = originType,
                        eventTime = validFrom,
                        expirationTime = validFrom.plusHours(legacyCredentials.attributes.validForHours),
                        validFrom = validFrom,
                    )
                )

                val domesticCredentials = listOf(legacyCredentials)

                val entities = domesticCredentials.map { domesticCredential ->
                    CredentialEntity(
                        greenCardId = localDomesticGreenCardId,
                        data = domesticCredential.credential.toString().replace("\\/", "/")
                            .toByteArray(),
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

                persistenceManager.deleteCredentials()
            } catch (exception: Exception) {
                // what to do if migration failed ?
            }
        }
    }
}
