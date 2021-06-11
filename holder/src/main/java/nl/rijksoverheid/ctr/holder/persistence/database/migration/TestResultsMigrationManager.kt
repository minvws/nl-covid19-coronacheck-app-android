package nl.rijksoverheid.ctr.holder.persistence.database.migration

import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.*
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import java.time.Instant
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

                val existingCredentialBytes = existingCredentials.toByteArray()

                val legacyCredentials = mobileCoreWrapper.readDomesticCredential(existingCredentialBytes)

                val validFrom = OffsetDateTime.ofInstant(Instant.ofEpochSecond(legacyCredentials.validFrom.toLong()), ZoneOffset.UTC)

                val originType = OriginType.Test
                holderDatabase.originDao().insert(
                    OriginEntity(
                        greenCardId = localDomesticGreenCardId,
                        type = originType,
                        eventTime = validFrom,
                        expirationTime = validFrom.plusHours(legacyCredentials.validForHours.toLong()),
                        validFrom = validFrom,
                    )
                )

                val legacyCredentialsList = listOf(legacyCredentials)

                val entities = legacyCredentialsList.map { domesticCredential ->
                    CredentialEntity(
                        greenCardId = localDomesticGreenCardId,
                        data = existingCredentialBytes,
                        credentialVersion = domesticCredential.credentialVersion.toInt(),
                        validFrom = OffsetDateTime.ofInstant(
                            Instant.ofEpochSecond(domesticCredential.validFrom.toLong()),
                            ZoneOffset.UTC
                        ),
                        expirationTime = OffsetDateTime.ofInstant(
                            Instant.ofEpochSecond(domesticCredential.validFrom.toLong()),
                            ZoneOffset.UTC
                        ).plusHours(domesticCredential.validForHours.toLong())
                    )
                }

                holderDatabase.credentialDao().insertAll(entities)
            } finally {
                persistenceManager.deleteCredentials()
            }
        }
    }
}
