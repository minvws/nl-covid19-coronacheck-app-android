package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import androidx.room.withTransaction
import io.mockk.*
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.dao.EventGroupDao
import nl.rijksoverheid.ctr.holder.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

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
        val remoteProtocol3 = createRemoteProtocol3(createVaccination())
        val byteArray = ByteArray(1)
        val remoteProtocols3 = mapOf(remoteProtocol3 to byteArray)

        runBlocking {
            saveEventsUseCaseImpl.saveRemoteProtocols3(
                remoteProtocols3,
                OriginType.Vaccination
            )

            coVerify { eventGroupDao.deleteAllOfType(OriginType.Vaccination) }
            coVerify {
                eventGroupDao.insertAll(
                    remoteProtocols3.map {
                        mapEventsToEntity(
                            it.key,
                            it.value,
                            OriginType.Vaccination
                        )
                    }
                )
            }
        }
    }

    @Test
    fun `when saving recoveries it should be inserted into the database with old one deleted`() {
        val remoteProtocol3 = createRemoteProtocol3(createRecovery())
        val byteArray = ByteArray(1)
        val remoteProtocols3 = mapOf(remoteProtocol3 to byteArray)

        runBlocking {
            saveEventsUseCaseImpl.saveRemoteProtocols3(
                remoteProtocols3,
                OriginType.Recovery
            )

            coVerify { eventGroupDao.deleteAllOfType(OriginType.Recovery) }
            coVerify {
                eventGroupDao.insertAll(
                    remoteProtocols3.map {
                        mapEventsToEntity(
                            it.key,
                            it.value,
                            OriginType.Recovery
                        )
                    }
                )
            }
        }
    }

    private fun mapEventsToEntity(
        remoteEvents: RemoteProtocol3,
        byteArray: ByteArray,
        eventType: OriginType
    ) = EventGroupEntity(
        walletId = 1,
        providerIdentifier = remoteEvents.providerIdentifier,
        type = eventType,
        maxIssuedAt = remoteEvents.events!!.first().getDate()!!,
        jsonData = byteArray
    )

    private fun createRemoteProtocol3(remoteEvent: RemoteEvent) = RemoteProtocol3(
        events = listOf(remoteEvent),
        protocolVersion = "pro",
        providerIdentifier = "ide",
        status = RemoteProtocol.Status.COMPLETE,
        holder = null
    )

    private fun createVaccination() = RemoteEventVaccination(
        type = null,
        unique = null,
        vaccination = RemoteEventVaccination.Vaccination(
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

    private fun createRecovery() = RemoteEventRecovery(
        type = null,
        unique = "uni",
        isSpecimen = true,
        recovery = RemoteEventRecovery.Recovery(
            sampleDate = LocalDate.of(2000, 1, 1),
            validFrom = LocalDate.of(2000, 2, 1),
            validUntil = LocalDate.of(2000, 7, 1)
        )
    )
}