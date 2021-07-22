package nl.rijksoverheid.ctr.holder.persistence.database.usecases

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.*
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteGreenCards
import nl.rijksoverheid.ctr.shared.models.DomesticCredential
import nl.rijksoverheid.ctr.shared.models.DomesticCredentialAttributes
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

@RunWith(RobolectricTestRunner::class)
class CreateDomesticGreenCardUseCaseImplTest: AutoCloseKoinTest() {

    private lateinit var db: HolderDatabase

    private val firstJanuaryDate = OffsetDateTime.ofInstant(Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC"))

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, HolderDatabase::class.java).build()
    }

    @Test
    fun `Remote domestic green card creates correct database models`() = runBlocking {
        val usecase = CreateDomesticGreenCardUseCaseImpl(
            holderDatabase = db,
        )

        val remoteGreenCard = RemoteGreenCards.DomesticGreenCard(
            origins = listOf(
                RemoteGreenCards.Origin(
                    type = OriginType.Vaccination,
                    eventTime = firstJanuaryDate,
                    expirationTime = firstJanuaryDate,
                    validFrom = firstJanuaryDate
                ),
                RemoteGreenCards.Origin(
                    type = OriginType.Test,
                    eventTime = firstJanuaryDate,
                    expirationTime = firstJanuaryDate,
                    validFrom = firstJanuaryDate
                ),
                RemoteGreenCards.Origin(
                    type = OriginType.Recovery,
                    eventTime = firstJanuaryDate,
                    expirationTime = firstJanuaryDate,
                    validFrom = firstJanuaryDate
                )
            ),
            createCredentialMessages = "".toByteArray()
        )

        val domesticCredentials = listOf(
            DomesticCredential(
                credential = JSONObject(),
                attributes = DomesticCredentialAttributes(
                    birthMonth = "1",
                    birthDay = "1",
                    credentialVersion = 1,
                    firstNameInitial = "B",
                    isSpecimen = "0",
                    lastNameInitial = "N",
                    isPaperProof = "0",
                    validForHours = 100,
                    validFrom = firstJanuaryDate.toEpochSecond()
                )
            )
        )

        db.walletDao().insert(
            WalletEntity(
                id = 1,
                label = "owner"
            )
        )

        usecase.create(remoteGreenCard, domesticCredentials)

        // We should have 1 green card
        val expectedGreenCard = GreenCard(
            greenCardEntity = GreenCardEntity(
                id = 1,
                walletId = 1,
                type = GreenCardType.Domestic
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
                OriginEntity(
                    id = 2,
                    greenCardId = 1,
                    type = OriginType.Test,
                    eventTime = firstJanuaryDate,
                    expirationTime = firstJanuaryDate,
                    validFrom = firstJanuaryDate
                ),
                OriginEntity(
                    id = 3,
                    greenCardId = 1,
                    type = OriginType.Recovery,
                    eventTime = firstJanuaryDate,
                    expirationTime = firstJanuaryDate,
                    validFrom = firstJanuaryDate
                ),
            ),
            credentialEntities = listOf(
                CredentialEntity(
                    id = 1,
                    greenCardId = 1,
                    data = JSONObject().toString().toByteArray(),
                    credentialVersion = 1,
                    validFrom = firstJanuaryDate,
                    expirationTime = firstJanuaryDate.plusHours(100)
                )
            )
        )

        assertEquals(expectedGreenCard, db.greenCardDao().getAll().first())
    }
}