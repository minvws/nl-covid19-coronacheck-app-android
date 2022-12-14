package nl.rijksoverheid.ctr.persistence

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import java.io.IOException
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.CredentialEntity
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardEntity
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginEntity
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.persistence.database.entities.SecretKeyEntity
import nl.rijksoverheid.ctr.persistence.database.entities.WalletEntity
import nl.rijksoverheid.ctr.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.persistence.database.models.Wallet
import org.junit.After
import org.junit.Assert.assertEquals
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
class HolderDatabaseTest : AutoCloseKoinTest() {

    private lateinit var db: HolderDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, HolderDatabase::class.java).build()
    }

    @After
    @Throws(IOException::class)
    fun teardown() {
        db.close()
    }

    @Test
    fun `Creating entities returns correct Wallet`() = runBlocking {
        val wallet = getDummyWallet()

        // Insert entities into database
        insertWalletInDatabase(
            wallet = wallet
        )

        // First wallet in database should match our dummy wallet object
        assertEquals(listOf(wallet), db.walletDao().get().first())
    }

    @Test
    fun `Events with different scopes can both exists in the database`() = runBlocking {
        val wallet = getDummyWallet()

        // Insert entities into database
        insertWalletInDatabase(
            wallet = wallet
        )

        val eventGroup1 = EventGroupEntity(
            id = 1,
            walletId = 1,
            providerIdentifier = "ggd",
            type = OriginType.Recovery,
            scope = "firstepisode",
            expiryDate = null,
            jsonData = "".toByteArray(),
            draft = false
        )

        val eventGroup2 = EventGroupEntity(
            id = 2,
            walletId = 1,
            providerIdentifier = "ggd",
            type = OriginType.Recovery,
            scope = "recovery",
            expiryDate = null,
            jsonData = "".toByteArray(),
            draft = false
        )

        db.eventGroupDao().insertAll(listOf(eventGroup1, eventGroup2))

        assertEquals("firstepisode", db.eventGroupDao().getAll()[0].scope)
        assertEquals("recovery", db.eventGroupDao().getAll()[1].scope)
    }

    @Test
    fun `Remove wallet clears correct database entries`() = runBlocking {
        val wallet = getDummyWallet()

        // Insert entities into database
        insertWalletInDatabase(
            wallet = wallet
        )

        // Delete the wallet entity
        db.walletDao().delete(1)

        // Database entries should no longer exist
        assertEquals(db.walletDao().get().first(), listOf<Wallet>())
        assertEquals(db.eventGroupDao().getAll(), listOf<EventGroupEntity>())
        assertEquals(db.greenCardDao().getAll(), listOf<GreenCardEntity>())
        assertEquals(db.credentialDao().getAll(), listOf<CredentialEntity>())
        assertEquals(db.originDao().getAll(), listOf<OriginEntity>())
    }

    @Test
    fun `Updating expiryDate updates field for event group`() = runBlocking {
        val eventGroup = EventGroupEntity(
            id = 1,
            walletId = 1,
            providerIdentifier = "ggd",
            type = OriginType.Recovery,
            scope = "firstepisode",
            expiryDate = null,
            jsonData = "".toByteArray(),
            draft = false
        )

        insertWalletInDatabase(
            wallet = getDummyWallet()
        )

        val expiryDate = OffsetDateTime.ofInstant(
            Instant.ofEpochSecond(1),
            ZoneOffset.UTC
        )

        db.eventGroupDao().insertAll(listOf(eventGroup))
        assertEquals(null, db.eventGroupDao().getAll().first().expiryDate)

        db.eventGroupDao().updateExpiryDate(
            eventGroupId = 1,
            expiryDate = expiryDate
        )

        assertEquals(expiryDate, db.eventGroupDao().getAll().first().expiryDate)
    }

    @Test
    fun `Removing green card removes secret key as well`() = runBlocking {
        val wallet = getDummyWallet()

        // Insert entities into database
        insertWalletInDatabase(
            wallet = wallet
        )

        // Insert secret key
        val secretKey = SecretKeyEntity(
            id = 1,
            greenCardId = 1,
            secretKey = "123"
        )
        db.secretKeyDao().insert(
            entity = secretKey
        )

        // We have a green card and a secret key
        val greenCard = db.greenCardDao().get(1)
        assertNotNull(greenCard)
        assertEquals(secretKey, db.secretKeyDao().get(1))

        // If we remove the green card
        db.greenCardDao().delete(greenCard)

        // The green card and the secret key is removed
        assertNull(db.greenCardDao().get(1))
        assertNull(db.secretKeyDao().get(1))
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
                    id = 1,
                    walletId = 1,
                    type = GreenCardType.Eu
                ),
                credentialEntities = listOf(
                    CredentialEntity(
                        id = 1,
                        greenCardId = 1,
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
                        id = 1,
                        greenCardId = 1,
                        type = OriginType.Recovery,
                        eventTime = OffsetDateTime.ofInstant(
                            Instant.ofEpochSecond(1),
                            ZoneOffset.UTC
                        ),
                        expirationTime = OffsetDateTime.ofInstant(
                            Instant.ofEpochSecond(1),
                            ZoneOffset.UTC
                        ),
                        OffsetDateTime.ofInstant(
                            Instant.ofEpochSecond(1),
                            ZoneOffset.UTC
                        )
                    )
                )
            ),
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
