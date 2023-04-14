package nl.rijksoverheid.ctr.persistence.database.usecases

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import nl.rijksoverheid.ctr.api.json.Base64JsonAdapter
import nl.rijksoverheid.ctr.holder.your_events.utils.SignedResponse
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
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@RunWith(RobolectricTestRunner::class)
class RemoveCTBUseCaseImplTest : AutoCloseKoinTest() {

    private lateinit var db: HolderDatabase

    private val eventTime =
        OffsetDateTime.now(Clock.fixed(Instant.parse("2023-06-25T09:00:00.00Z"), ZoneId.of("UTC")))
            .toEpochSecond()
    private val eventExpirationTime =
        OffsetDateTime.now(Clock.fixed(Instant.parse("2023-12-25T09:00:00.00Z"), ZoneId.of("UTC")))
            .toEpochSecond()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(Base64JsonAdapter())
        .build()

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
                    jsonData().toByteArray()
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

            val removeCTBUseCase = RemoveCTBUseCaseImpl(
                moshi = moshi,
                holderDatabase = db
            )
            removeCTBUseCase.execute()

            assertEquals(2, db.eventGroupDao().getAll().size)
            assertEquals(1, db.greenCardDao().getAll().size)
        }

    private fun jsonData(): String {
        val payload =
            "WwogeyJhZm5hbWVkYXR1bSI6IjIwMjAtMDYtMTdUMTA6MDA6MDAuMDAwKzAyMDAiLAogICJ1aXRzbGFnZGF0dW0iOiIyMDIwLTA2LTE3VDEwOjEwOjAwLjAwMCswMjAwIiwKICAicmVzdWx0YWF0IjoiTkVHQVRJRUYiLAogICJhZnNwcmFha1N0YXR1cyI6IkFGR0VST05EIiwKICAiYWZzcHJhYWtJZCI6Mjc4NzE3Njh9LAogeyJhZm5hbWVkYXR1bSI6IjIwMjAtMTEtMDhUMTA6MTU6MDAuMDAwKzAxMDAiLAogICAidWl0c2xhZ2RhdHVtIjoiMjAyMC0xMS0wOVQwNzo1MDozOS4wMDArMDEwMCIsCiAgICJyZXN1bHRhYXQiOiJQT1NJVElFRiIsCiAgICJhZnNwcmFha1N0YXR1cyI6IkFGR0VST05EIiwKICAgImFmc3ByYWFrSWQiOjI1ODcxOTcyMTl9Cl0K"
        val signature =
            "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"
        return moshi.adapter(SignedResponse::class.java).toJson(
            SignedResponse(
                signature = signature,
                payload = payload
            )
        )
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
                jsonData = jsonData().toByteArray(),
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
                jsonData = jsonData().toByteArray(),
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
