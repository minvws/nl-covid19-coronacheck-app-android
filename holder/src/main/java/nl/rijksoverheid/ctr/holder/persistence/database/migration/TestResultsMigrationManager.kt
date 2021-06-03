package nl.rijksoverheid.ctr.holder.persistence.database.migration

import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.CoronaCheckRepository
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.SecretKeyUseCase
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import nl.rijksoverheid.ctr.shared.models.DomesticCredential
import nl.rijksoverheid.ctr.shared.models.DomesticCredentialAttributes
import org.json.JSONObject
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

interface TestResultsMigrationManager {
    suspend fun migrateTestResults()
}

class TestResultsMigrationManagerImpl(private val persistenceManager: PersistenceManager,
                                      private val mobileCoreWrapper: MobileCoreWrapper,
                                      private val holderDatabase: HolderDatabase,
                                      private val coronaCheckRepository: CoronaCheckRepository,
                                      private val secretKeyUseCase: SecretKeyUseCase): TestResultsMigrationManager {
    override suspend fun migrateTestResults() {

        val existingCredentials1 = persistenceManager.getCredentials()
        if (existingCredentials1 != null) {
            val existingCredentials = persistenceManager.getCredentials()!!
            val testAttributes = mobileCoreWrapper.readDomesticCredential(existingCredentials.toByteArray())
            println("GIO pare: $testAttributes")

            val localDomesticGreenCardId = holderDatabase.greenCardDao().insert(
                GreenCardEntity(
                    walletId = 1,
                    type = GreenCardType.Domestic
                )
            )
            println("GIO 1")
            val originType = OriginType.Test
            holderDatabase.originDao().insert(
                OriginEntity(
                    greenCardId = localDomesticGreenCardId,
                    type = originType,
                    eventTime = OffsetDateTime.now(),// replace with sampleTime from the test
                    expirationTime = OffsetDateTime.now().plusDays(2), // replace with sampleTime from the test
                    validFrom = OffsetDateTime.now().minusDays(1), //replace with validFrom from the tst
                )
            )

            println("GIO 2")

            val prepareIssue = coronaCheckRepository.getPrepareIssue()

            val commitmentMessage = mobileCoreWrapper.createCommitmentMessage(
                secretKey = secretKeyUseCase.json().toByteArray(),
                prepareIssueMessage = prepareIssue.prepareIssueMessage
            )

            println("GIO 2 2")

//            val domesticCredentials = mobileCoreWrapper.createDomesticCredentials(
//                createCredentials = existingCredentials.toByteArray()
//            )

            val domesticCredentials = listOf(DomesticCredential(
                JSONObject().apply {
                                   put("birthDay", testAttributes.birthDay)
                                   put("birthMonth", testAttributes.birthMonth)
                                   put("credentialVersion", testAttributes.credentialVersion)
                                   put("firstNameInitial", testAttributes.firstNameInitial)
                                   put("isSpecimen", testAttributes.isSpecimen)
                                   put("lastNameInitial", testAttributes.lastNameInitial)
                                   put("stripType", testAttributes.stripType)
                                   put("validForHours", 72)
                                   put("validFrom", 1622710800)
                }, DomesticCredentialAttributes(
                    birthDay = testAttributes.birthDay,
                    birthMonth = testAttributes.birthMonth,
                    credentialVersion = 1,
                    firstNameInitial = testAttributes.firstNameInitial,
                    isSpecimen = testAttributes.isSpecimen,
                    lastNameInitial = testAttributes.lastNameInitial,
                    stripType = testAttributes.stripType,
                    validForHours = 72,
                    validFrom = 1622710800,
                )
            ))
            println("GIO domesticCredentials $domesticCredentials")

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

            println("GIO 4")

            holderDatabase.credentialDao().insertAll(entities)

            println("GIO 5")
        }
    }
}
