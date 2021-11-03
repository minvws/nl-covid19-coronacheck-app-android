package nl.rijksoverheid.ctr.holder.persistence.database.usecases

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.*
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteGreenCards
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@RunWith(RobolectricTestRunner::class)
class CreateEuGreenCardUseCaseImplTest : AutoCloseKoinTest() {

    private lateinit var db: HolderDatabase
    private val mobileCoreWrapper = mockk<MobileCoreWrapper>(relaxed = true)

    private val firstJanuaryDate =
        OffsetDateTime.ofInstant(Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC"))

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, HolderDatabase::class.java).build()
    }

    @Test
    fun `Remote eu green card creates correct database models`() = runBlocking {
        val usecase = CreateEuGreenCardUseCaseImpl(
            holderDatabase = db,
            mobileCoreWrapper = mobileCoreWrapper
        )

        val credentialJson = JSONObject()
        credentialJson.put("credentialVersion", 1)
        credentialJson.put("issuedAt", firstJanuaryDate.toEpochSecond())
        credentialJson.put("expirationTime", firstJanuaryDate.toEpochSecond())

        val remoteGreenCard = RemoteGreenCards.EuGreenCard(
            origins = listOf(
                RemoteGreenCards.Origin(
                    type = OriginType.Vaccination,
                    eventTime = firstJanuaryDate,
                    expirationTime = firstJanuaryDate,
                    validFrom = firstJanuaryDate
                )
            ),
            credential = credentialJson.toString()
        )

        coEvery { mobileCoreWrapper.readEuropeanCredential(any()) } answers {
            credentialJson
        }

        db.walletDao().insert(
            WalletEntity(
                id = 1,
                label = "owner"
            )
        )

        usecase.create(remoteGreenCard)

        // We should have 3 green cards
        val euVaccinationGreenCard = GreenCard(
            greenCardEntity = GreenCardEntity(
                id = 1,
                walletId = 1,
                type = GreenCardType.Eu
            ),
            origins = listOf(
                OriginEntity(
                    id = 1,
                    greenCardId = 1,
                    type = OriginType.Vaccination,
                    eventTime = firstJanuaryDate,
                    expirationTime = firstJanuaryDate,
                    validFrom = firstJanuaryDate
                ),
            ),
            credentialEntities = listOf(
                CredentialEntity(
                    id = 1,
                    greenCardId = 1,
                    data = credentialJson.toString().toByteArray(),
                    credentialVersion = 1,
                    validFrom = firstJanuaryDate,
                    expirationTime = firstJanuaryDate
                )
            )
        )
        val greenCards = db.greenCardDao().getAll()

        assertEquals(euVaccinationGreenCard, greenCards[0])
    }
}