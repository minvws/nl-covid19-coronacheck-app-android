package nl.rijksoverheid.ctr.persistence.database.usecases

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.CredentialEntity
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardEntity
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginEntity
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.persistence.database.entities.WalletEntity
import nl.rijksoverheid.ctr.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.persistence.database.models.Wallet
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@RunWith(RobolectricTestRunner::class)
class RemoveCTBUseCaseImplTest {

    private lateinit var db: HolderDatabase

    private val eventTime =
        OffsetDateTime.now(Clock.fixed(Instant.parse("2023-06-25T09:00:00.00Z"), ZoneId.of("UTC")))
            .toEpochSecond()
    private val eventExpirationTime =
        OffsetDateTime.now(Clock.fixed(Instant.parse("2023-12-25T09:00:00.00Z"), ZoneId.of("UTC")))
            .toEpochSecond()

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, HolderDatabase::class.java)
            .allowMainThreadQueries().build()
    }

    @After
    @Throws(IOException::class)
    fun teardown() {
        db.close()
    }

    @Test
    fun `given a db with domestic and eu data, when remove the domestic data, only eu data remain`() =
        runTest {
            val wallet = getDummyWallet()
            insertWalletInDatabase(
                wallet = wallet
            )
            val writeableDb = db.openHelper.writableDatabase
            val greenCardId = 1
            writeableDb.execSQL(
                "INSERT INTO event_group (id, wallet_id, provider_identifier, type, scope, expiryDate, draft, jsonData) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                arrayOf(
                    3,
                    wallet.walletEntity.id,
                    "1",
                    "vaccinationassessment",
                    "",
                    null,
                    0,
                    "".toByteArray()
                )
            )
            writeableDb.execSQL(
                "INSERT INTO green_card (id, wallet_id, type) VALUES (?, ?, ?)",
                arrayOf(greenCardId, wallet.walletEntity.id, "domestic")
            )
            writeableDb.execSQL(
                "INSERT INTO origin (id, green_card_id, type, eventTime, expirationTime, validFrom) VALUES (?, ?, ?, ?, ?, ?)",
                arrayOf(
                    1,
                    greenCardId,
                    "vaccinationassessment",
                    eventTime,
                    eventExpirationTime,
                    eventTime
                )
            )
            writeableDb.execSQL(
                "INSERT INTO credential (id, green_card_id, data, credentialVersion, validFrom, expirationTime) VALUES (?, ?, ?, ?, ?, ?)",
                arrayOf(10, greenCardId, "".toByteArray(), 1, eventTime, eventExpirationTime)
            )
            writeableDb.execSQL(
                "INSERT INTO credential (id, green_card_id, data, credentialVersion, validFrom, expirationTime) VALUES (?, ?, ?, ?, ?, ?)",
                arrayOf(11, greenCardId, "".toByteArray(), 1, eventTime, eventExpirationTime)
            )

            val removeCTBUseCase = RemoveCTBUseCaseImpl(db)
            removeCTBUseCase.execute()

            assertEquals(2, db.eventGroupDao().getAll().size)
            assertEquals(1, db.greenCardDao().getAll().size)
        }

    private fun getDummyWallet() = Wallet(
        walletEntity = WalletEntity(
            id = 1,
            label = "main"
        ),
        eventEntities = listOf(
            EventGroupEntity(
                id = 1,
                walletId = 1,
                type = OriginType.Vaccination,
                expiryDate = null,
                jsonData = "".toByteArray(),
                scope = "",
                providerIdentifier = "1",
                draft = false
            ),
            EventGroupEntity(
                id = 2,
                walletId = 1,
                type = OriginType.Vaccination,
                expiryDate = null,
                scope = "",
                jsonData = "".toByteArray(),
                providerIdentifier = "2",
                draft = false
            )
        ),
        greenCards = listOf(
            GreenCard(
                greenCardEntity = GreenCardEntity(
                    id = 2,
                    walletId = 1,
                    type = GreenCardType.Eu
                ),
                credentialEntities = listOf(
                    CredentialEntity(
                        id = 2,
                        greenCardId = 2,
                        data = "".toByteArray(),
                        credentialVersion = 1,
                        validFrom = OffsetDateTime.ofInstant(
                            Instant.ofEpochSecond(1),
                            ZoneOffset.UTC
                        ),
                        expirationTime = OffsetDateTime.ofInstant(
                            Instant.ofEpochSecond(1),
                            ZoneOffset.UTC
                        )
                    )
                ),
                origins = listOf(
                    OriginEntity(
                        id = 2,
                        greenCardId = 2,
                        type = OriginType.Test,
                        eventTime = OffsetDateTime.ofInstant(
                            Instant.ofEpochSecond(1),
                            ZoneOffset.UTC
                        ),
                        expirationTime = OffsetDateTime.ofInstant(
                            Instant.ofEpochSecond(1),
                            ZoneOffset.UTC
                        ),
                        validFrom = OffsetDateTime.ofInstant(
                            Instant.ofEpochSecond(1),
                            ZoneOffset.UTC
                        )
                    )
                )
            )
        )
    )

    private suspend fun insertWalletInDatabase(wallet: Wallet) {
        db.walletDao().insert(wallet.walletEntity)
        db.eventGroupDao().insertAll(wallet.eventEntities)
        wallet.greenCards.forEach { greenCard ->
            db.greenCardDao().insert(greenCard.greenCardEntity)
            db.originDao().insertAll(greenCard.origins)
            greenCard.credentialEntities.forEach {
                db.credentialDao().insert(it)
            }
        }
    }
}
