/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.your_events.usecases

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.mockk.coEvery
import io.mockk.mockk
import java.time.OffsetDateTime
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccination
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol
import nl.rijksoverheid.ctr.holder.models.HolderFlow
import nl.rijksoverheid.ctr.holder.your_events.models.ConflictingEventResult
import nl.rijksoverheid.ctr.holder.your_events.usecases.SaveEventsUseCase
import nl.rijksoverheid.ctr.holder.your_events.utils.RemoteEventHolderUtil
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.persistence.database.entities.WalletEntity
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SaveEventsUseCaseImplTest : AutoCloseKoinTest() {

    @Before
    fun setup() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, HolderDatabase::class.java).build()
        db.walletDao().insert(
            WalletEntity(
                id = 1,
                label = "Wallet"
            )
        )

        loadKoinModules(
            module(override = true) {
                single {
                    db
                }
            }
        )
    }

    @Test
    fun `saveRemoteProtocols3 correctly saves ggd events`() = runBlocking {
        val remoteProtocol = RemoteProtocol(
            providerIdentifier = "ggd",
            protocolVersion = "1",
            status = RemoteProtocol.Status.COMPLETE,
            holder = null,
            events = listOf(
                RemoteEventVaccination(
                    type = "vaccination",
                    unique = "",
                    vaccination = null
                )
            )
        )

        val db: HolderDatabase by inject()
        val usecase: SaveEventsUseCase by inject()

        usecase.saveRemoteProtocols3(
            remoteProtocols = mapOf(
                remoteProtocol to "".toByteArray()
            ),
            removePreviousEvents = false,
            flow = HolderFlow.SyncGreenCards
        )

        val events = db.eventGroupDao().getAll()
        assertEquals(1, events.size)
        assertEquals("ggd", events.first().providerIdentifier)
    }

    @Test
    fun `saveRemoteProtocols3 correctly saves zzz events`() = runBlocking {
        val remoteProtocol = RemoteProtocol(
            providerIdentifier = "zzz",
            protocolVersion = "1",
            status = RemoteProtocol.Status.COMPLETE,
            holder = null,
            events = listOf(
                RemoteEventVaccination(
                    type = "vaccination",
                    unique = "123",
                    vaccination = null
                ),
                RemoteEventVaccination(
                    type = "vaccination",
                    unique = "456",
                    vaccination = null
                )
            )
        )

        val db: HolderDatabase by inject()
        val usecase: SaveEventsUseCase by inject()

        usecase.saveRemoteProtocols3(
            remoteProtocols = mapOf(
                remoteProtocol to "".toByteArray()
            ),
            removePreviousEvents = false,
            flow = HolderFlow.SyncGreenCards
        )

        val events = db.eventGroupDao().getAll()
        assertEquals(1, events.size)
        assertEquals("zzz_123456", events.first().providerIdentifier)
    }

    @Test
    fun `saveRemoteProtocols3 removes previous events if removePreviousEvents is true`() = runBlocking {
        // Insert event into database
        val db: HolderDatabase by inject()
        val eventGroupEntity = EventGroupEntity(
            id = 0,
            walletId = 1,
            providerIdentifier = "ggd",
            type = OriginType.Test,
            scope = "",
            expiryDate = OffsetDateTime.now(),
            jsonData = "".toByteArray()
        )
        db.eventGroupDao().insertAll(listOf(eventGroupEntity))
        val eventGroups = db.eventGroupDao().getAll()
        assertEquals(1, eventGroups.size)
        assertEquals("ggd", eventGroups.first().providerIdentifier)

        // When calling usecase with removePreviousEvents to true, it should remove previous events
        val usecase: SaveEventsUseCase by inject()
        usecase.saveRemoteProtocols3(
            remoteProtocols = mapOf(),
            removePreviousEvents = true,
            flow = HolderFlow.SyncGreenCards
        )
        val newEventGroups = db.eventGroupDao().getAll()
        assertEquals(0, newEventGroups.size)
    }

    @Test
    fun `remoteProtocols3AreConflicting returns ConflictingEventResultHolder if holders are conflicting`() = runBlocking {
        // Mock remoteEventHolderUtil because of signed json blob
        val remoteEventHolderUtil: RemoteEventHolderUtil = mockk(relaxed = true)
        coEvery { remoteEventHolderUtil.conflicting(any(), any()) } answers { true }
        loadKoinModules(
            module(override = true) {
                factory {
                    remoteEventHolderUtil
                }
            }
        )

        // Insert event into database
        val db: HolderDatabase by inject()
        val eventGroupEntity1 = EventGroupEntity(
            id = 0,
            walletId = 1,
            providerIdentifier = "ggd",
            type = OriginType.Vaccination,
            scope = "",
            expiryDate = OffsetDateTime.now(),
            jsonData = "".toByteArray()
        )
        db.eventGroupDao().insertAll(listOf(eventGroupEntity1))

        // Check with remote protocol
        val usecase: SaveEventsUseCase by inject()
        val conflicting = usecase.remoteProtocols3AreConflicting(
            remoteProtocols = mapOf(
                RemoteProtocol(
                    providerIdentifier = "ggd",
                    protocolVersion = "1",
                    status = RemoteProtocol.Status.COMPLETE,
                    holder = null,
                    events = listOf()
                ) to "".toByteArray()
            )
        )
        assertEquals(ConflictingEventResult.Holder, conflicting)
    }

    @Test
    fun `remoteProtocols3AreConflicting returns ConflictingEventResultNone if remote protocols are not conflicting`() = runBlocking {
        // Mock remoteEventHolderUtil because of signed json blob
        val remoteEventHolderUtil: RemoteEventHolderUtil = mockk(relaxed = true)
        coEvery { remoteEventHolderUtil.conflicting(any(), any()) } answers { false }
        loadKoinModules(
            module(override = true) {
                factory {
                    remoteEventHolderUtil
                }
            }
        )

        // Insert event into database
        val db: HolderDatabase by inject()
        val eventGroupEntity1 = EventGroupEntity(
            id = 0,
            walletId = 1,
            providerIdentifier = "ggd",
            type = OriginType.Vaccination,
            scope = "",
            expiryDate = OffsetDateTime.now(),
            jsonData = "".toByteArray()
        )
        db.eventGroupDao().insertAll(listOf(eventGroupEntity1))

        // Check with remote protocol
        val usecase: SaveEventsUseCase by inject()
        val conflicting = usecase.remoteProtocols3AreConflicting(
            remoteProtocols = mapOf(
                RemoteProtocol(
                    providerIdentifier = "ggd",
                    protocolVersion = "1",
                    status = RemoteProtocol.Status.COMPLETE,
                    holder = null,
                    events = listOf()
                ) to "".toByteArray()
            )
        )
        assertEquals(ConflictingEventResult.None, conflicting)
    }

    @Test
    fun `remoteProtocols3AreConflicting returns ConflictingEventResultExisting if remote protocols are conflicting`() = runBlocking {
        // Mock remoteEventHolderUtil because of signed json blob
        val remoteEventHolderUtil: RemoteEventHolderUtil = mockk(relaxed = true)
        coEvery { remoteEventHolderUtil.conflicting(any(), any()) } answers { false }
        loadKoinModules(
            module(override = true) {
                factory {
                    remoteEventHolderUtil
                }
            }
        )

        // Insert event into database
        val db: HolderDatabase by inject()
        val eventGroupEntity1 = EventGroupEntity(
            id = 0,
            walletId = 1,
            providerIdentifier = "dcc_unique",
            type = OriginType.Vaccination,
            scope = "",
            expiryDate = OffsetDateTime.now(),
            jsonData = "".toByteArray()
        )
        db.eventGroupDao().insertAll(listOf(eventGroupEntity1))

        // Check with remote protocol
        val usecase: SaveEventsUseCase by inject()
        val conflicting = usecase.remoteProtocols3AreConflicting(
            remoteProtocols = mapOf(
                RemoteProtocol(
                    providerIdentifier = "dcc",
                    protocolVersion = "1",
                    status = RemoteProtocol.Status.COMPLETE,
                    holder = null,
                    events = listOf(
                        RemoteEventVaccination(
                            "vaccination", "unique",
                            vaccination = null
                        )
                    )
                ) to "".toByteArray()
            )
        )
        assertEquals(ConflictingEventResult.Existing, conflicting)
    }
}
