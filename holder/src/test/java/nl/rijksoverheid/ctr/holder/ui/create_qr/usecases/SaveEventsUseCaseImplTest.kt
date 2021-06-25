package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import androidx.room.withTransaction
import io.mockk.*
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.dao.EventGroupDao
import nl.rijksoverheid.ctr.holder.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.EventType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventsVaccinations
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteProtocol
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset.UTC

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class SaveEventsUseCaseImplTest {

    private val eventGroupDao: EventGroupDao = mockk(relaxed = true)
    private val holderDatabase: HolderDatabase = mockk {
        every { eventGroupDao() } returns eventGroupDao
    }

    private val saveEventsUseCaseImpl = SaveEventsUseCaseImpl(holderDatabase)

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkStatic(
            "androidx.room.RoomDatabaseKt"
        )

        val transactionLambda = slot<suspend () -> Unit>()
        coEvery { holderDatabase.withTransaction(capture(transactionLambda)) } coAnswers {
            transactionLambda.captured.invoke()
        }
    }

    @Test
    fun `when saving vaccinations it should be inserted into the database with old one deleted`() {
        val remoteEvents = createRemoteEvents()
        val byteArray = ByteArray(1)
        val vaccinations = mapOf(remoteEvents to byteArray)

        runBlocking {
            saveEventsUseCaseImpl.saveVaccinations(vaccinations)

            coVerify { eventGroupDao.deleteAllOfType(EventType.Vaccination) }
            coVerify {
                eventGroupDao.insertAll(
                    listOf(
                        mapEventsToEntity(remoteEvents, byteArray)
                    )
                )
            }
        }
    }

    private fun mapEventsToEntity(
        remoteEvents: RemoteEventsVaccinations,
        byteArray: ByteArray
    ) = EventGroupEntity(
        walletId = 1,
        providerIdentifier = remoteEvents.providerIdentifier,
        type = EventType.Vaccination,
        maxIssuedAt = remoteEvents.events!!.first().vaccination!!.date!!.atStartOfDay()
            .atOffset(UTC)!!,
        jsonData = byteArray
    )

    private fun createRemoteEvents() = RemoteEventsVaccinations(
        events = listOf(
            RemoteEventsVaccinations.Event(
                type = null,
                unique = null,
                vaccination = RemoteEventsVaccinations.Event.Vaccination(
                    date = LocalDate.of(2000, 1, 1),
                    hpkCode = null,
                    type = null,
                    brand = null,
                    completedByMedicalStatement = null,
                    doseNumber = null,
                    totalDoses = null,
                    country = null,
                    manufacturer = null,
                )
            )
        ),
        protocolVersion = "pro",
        providerIdentifier = "ide",
        status = RemoteProtocol.Status.COMPLETE,
        holder = null
    )
}