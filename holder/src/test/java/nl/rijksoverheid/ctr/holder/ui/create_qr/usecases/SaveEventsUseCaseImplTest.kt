package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.holder.HolderFlow
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.dao.EventGroupDao
import nl.rijksoverheid.ctr.holder.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.RemoteEventHolderUtil
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.RemoteEventUtil
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.ScopeUtilImpl
import nl.rijksoverheid.ctr.shared.models.Flow
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@RunWith(RobolectricTestRunner::class)
class SaveEventsUseCaseImplTest {

    private val eventGroupDao: EventGroupDao = mockk(relaxed = true)
    private val holderDatabase: HolderDatabase = mockk {
        every { eventGroupDao() } returns eventGroupDao
    }
    private val scopeUtil = ScopeUtilImpl()
    private val remoteEventUtil: RemoteEventUtil = mockk()

    private val remoteEventHolderUtil: RemoteEventHolderUtil = mockk(relaxed = true)

    @After
    fun tearDown() {
        stopKoin()
    }

    private val saveEventsUseCaseImpl = SaveEventsUseCaseImpl(
        holderDatabase, remoteEventHolderUtil,
        scopeUtil, remoteEventUtil
    )

    @Test
    fun `when saving vaccinations it should be inserted into the database with old one deleted`() {
        val remoteProtocol3 = createRemoteProtocol3(createVaccination())
        val byteArray = ByteArray(1)
        val remoteProtocols3 = mapOf(remoteProtocol3 to byteArray)
        every { remoteEventUtil.getOriginType(remoteProtocol3.events!!.first()) } returns OriginType.Vaccination

        runBlocking {
            saveEventsUseCaseImpl.saveRemoteProtocols3(
                remoteProtocols3,
                true,
                HolderFlow.Vaccination
            )

            coVerify { eventGroupDao.deleteAll() }
            coVerify {
                eventGroupDao.insertAll(
                    remoteProtocols3.map {
                        mapEventsToEntity(
                            it.key,
                            it.value,
                            OriginType.Vaccination,
                            HolderFlow.Vaccination
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
        every { remoteEventUtil.getOriginType(remoteProtocol3.events!!.first()) } returns OriginType.Recovery

        runBlocking {
            saveEventsUseCaseImpl.saveRemoteProtocols3(
                remoteProtocols3,
                true,
                HolderFlow.Recovery
            )

            coVerify { eventGroupDao.deleteAll() }
            coVerify {
                eventGroupDao.insertAll(
                    remoteProtocols3.map {
                        mapEventsToEntity(
                            it.key,
                            it.value,
                            OriginType.Recovery,
                            HolderFlow.Recovery
                        )
                    }
                )
            }
        }
    }

    @Test
    fun `when saving recovery and vaccination it should both be inserted into the database with old one deleted`() {
        val remoteProtocol3Recovery = createRemoteProtocol3(createRecovery())
        val byteArrayRecovery = ByteArray(1)
        val remoteProtocols3Recovery = mapOf(remoteProtocol3Recovery to byteArrayRecovery)

        val remoteProtocol3Vaccination = createRemoteProtocol3(createVaccination())
        val byteArray2Vaccination = ByteArray(1)
        val remoteProtocols3Vaccination = mapOf(remoteProtocol3Vaccination to byteArray2Vaccination)

        val remoteProtocols3 = remoteProtocols3Recovery + remoteProtocols3Vaccination
        val entities =
            remoteProtocols3Recovery.map {
                mapEventsToEntity(
                    it.key,
                    it.value,
                    OriginType.Recovery,
                    HolderFlow.VaccinationAndPositiveTest
                )
            } + remoteProtocols3Vaccination.map {
                mapEventsToEntity(
                    it.key,
                    it.value,
                    OriginType.Vaccination,
                    HolderFlow.VaccinationAndPositiveTest
                )
            }
        every { remoteEventUtil.getOriginType(remoteProtocols3.keys.first().events!!.first()) } returns OriginType.Recovery
        every { remoteEventUtil.getOriginType(remoteProtocols3.keys.elementAt(1).events!!.first()) } returns OriginType.Vaccination

        runBlocking {
            saveEventsUseCaseImpl.saveRemoteProtocols3(
                remoteProtocols3,
                true,
                HolderFlow.VaccinationAndPositiveTest
            )

            coVerify { eventGroupDao.deleteAll() }
            coVerify { eventGroupDao.insertAll(entities) }
        }
    }

    private fun mapEventsToEntity(
        remoteEvents: RemoteProtocol3,
        byteArray: ByteArray,
        eventType: OriginType,
        flow: Flow
    ) = EventGroupEntity(
        walletId = 1,
        providerIdentifier = remoteEvents.providerIdentifier,
        type = eventType,
        maxIssuedAt = remoteEvents.events!!.first().getDate()!!,
        jsonData = byteArray,
        scope = scopeUtil.getScopeForOriginType(
            eventType,
            flow == HolderFlow.VaccinationAndPositiveTest
        )
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
            completedByPersonalStatement = null,
            completionReason = null
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