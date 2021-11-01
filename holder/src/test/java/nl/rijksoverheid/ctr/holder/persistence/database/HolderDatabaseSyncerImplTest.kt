package nl.rijksoverheid.ctr.holder.persistence.database

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.holder.*
import nl.rijksoverheid.ctr.holder.persistence.database.dao.EventGroupDao
import nl.rijksoverheid.ctr.holder.persistence.database.dao.GreenCardDao
import nl.rijksoverheid.ctr.holder.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.persistence.database.usecases.RemoteGreenCardsResult
import nl.rijksoverheid.ctr.holder.persistence.database.usecases.SyncRemoteGreenCardsResult
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteGreenCards
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.GreenCardUtil
import nl.rijksoverheid.ctr.shared.models.AppErrorResult
import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult
import nl.rijksoverheid.ctr.shared.models.Step
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.lang.IllegalStateException
import java.time.OffsetDateTime

class HolderDatabaseSyncerImplTest {

    private val greenCardDao = mockk<GreenCardDao>(relaxed = true)
    private val eventGroupDao = mockk<EventGroupDao>(relaxed = true)
    private val holderDatabase = mockk<HolderDatabase>(relaxed = true).apply {
        coEvery { eventGroupDao() } returns eventGroupDao
        coEvery { greenCardDao() } returns greenCardDao
    }
    private val events = listOf(
        EventGroupEntity(
            id = 1,
            walletId = 1,
            providerIdentifier = "1",
            type = OriginType.Test,
            maxIssuedAt = OffsetDateTime.now(),
            jsonData = "".toByteArray()
        )
    )

    @Test
    fun `sync returns Success if has no events`() = runBlocking {
        coEvery { eventGroupDao.getAll() } answers { listOf() }

        val holderDatabaseSyncer = HolderDatabaseSyncerImpl(
            holderDatabase = holderDatabase,
            greenCardUtil = fakeGreenCardUtil(),
            getRemoteGreenCardsUseCase = fakeGetRemoteGreenCardUseCase(
                result = successResult(
                    originType = OriginType.Test
                )
            ),
            syncRemoteGreenCardsUseCase = fakeSyncRemoteGreenCardUseCase(),
            removeExpiredEventsUseCase = fakeRemoveExpiredEventsUseCase(),
            persistenceManager = fakePersistenceManager()
        )

        val databaseSyncerResult = holderDatabaseSyncer.sync(
            expectedOriginType = OriginType.Test,
            syncWithRemote = true
        )

        assertEquals(DatabaseSyncerResult.Success(), databaseSyncerResult)
    }

    @Test
    fun `sync returns Success if has events and nothing errors`() = runBlocking {
        coEvery { eventGroupDao.getAll() } answers { events }

        val holderDatabaseSyncer = HolderDatabaseSyncerImpl(
            holderDatabase = holderDatabase,
            greenCardUtil = fakeGreenCardUtil(),
            getRemoteGreenCardsUseCase = fakeGetRemoteGreenCardUseCase(
                result = successResult(
                    originType = OriginType.Test
                )
            ),
            syncRemoteGreenCardsUseCase = fakeSyncRemoteGreenCardUseCase(),
            removeExpiredEventsUseCase = fakeRemoveExpiredEventsUseCase(),
            persistenceManager = fakePersistenceManager()
        )

        val databaseSyncerResult = holderDatabaseSyncer.sync(
            expectedOriginType = OriginType.Test,
            syncWithRemote = true
        )

        assertEquals(DatabaseSyncerResult.Success(), databaseSyncerResult)
    }

    @Test
    fun `sync returns Success with missingOrigin if returned origins do not match expected origin`() = runBlocking {
        coEvery { eventGroupDao.getAll() } answers { events }

        val holderDatabaseSyncer = HolderDatabaseSyncerImpl(
            holderDatabase = holderDatabase,
            greenCardUtil = fakeGreenCardUtil(),
            getRemoteGreenCardsUseCase = fakeGetRemoteGreenCardUseCase(
                result = RemoteGreenCardsResult.Success(
                    remoteGreenCards = RemoteGreenCards(
                        domesticGreencard = RemoteGreenCards.DomesticGreenCard(
                            origins = listOf(RemoteGreenCards.Origin(
                                type = OriginType.Vaccination,
                                eventTime = OffsetDateTime.now(),
                                expirationTime = OffsetDateTime.now(),
                                validFrom = OffsetDateTime.now()
                            )),
                            createCredentialMessages = "".toByteArray()
                        ),
                        euGreencards = null
                    )
                )
            ),
            syncRemoteGreenCardsUseCase = fakeSyncRemoteGreenCardUseCase(),
            removeExpiredEventsUseCase = fakeRemoveExpiredEventsUseCase(),
            persistenceManager = fakePersistenceManager()
        )

        val databaseSyncerResult = holderDatabaseSyncer.sync(
            expectedOriginType = OriginType.Test,
            syncWithRemote = true
        )

        assertEquals(DatabaseSyncerResult.Success(true), databaseSyncerResult)
    }

    @Test
    fun `sync returns NetworkError if getting remote green cards returns NetworkError`() = runBlocking {
        coEvery { eventGroupDao.getAll() } answers { events }
        coEvery { greenCardDao.getAll() } answers { listOf() }

        val holderDatabaseSyncer = HolderDatabaseSyncerImpl(
            holderDatabase = holderDatabase,
            greenCardUtil = fakeGreenCardUtil(),
            getRemoteGreenCardsUseCase = fakeGetRemoteGreenCardUseCase(
                result = RemoteGreenCardsResult.Error(NetworkRequestResult.Failed.ServerNetworkError(Step(1), IllegalStateException("Some error")))),
            syncRemoteGreenCardsUseCase = fakeSyncRemoteGreenCardUseCase(
                result = SyncRemoteGreenCardsResult.Success
            ),
            removeExpiredEventsUseCase = fakeRemoveExpiredEventsUseCase(),
            persistenceManager = fakePersistenceManager()
        )

        val databaseSyncerResult = holderDatabaseSyncer.sync(
            expectedOriginType = OriginType.Test,
            syncWithRemote = true
        )

        assertTrue(databaseSyncerResult is DatabaseSyncerResult.Failed.NetworkError)
    }

    @Test
    fun `sync returns ServerError if getting remote green cards returns ServerError`() = runBlocking {
        coEvery { eventGroupDao.getAll() } answers { events }
        coEvery { greenCardDao.getAll() } answers { listOf() }

        val holderDatabaseSyncer = HolderDatabaseSyncerImpl(
            holderDatabase = holderDatabase,
            greenCardUtil = fakeGreenCardUtil(),
            getRemoteGreenCardsUseCase = fakeGetRemoteGreenCardUseCase(
                result = RemoteGreenCardsResult.Error(NetworkRequestResult.Failed.CoronaCheckHttpError(Step(1), HttpException(
                    Response.error<String>(
                        400, "".toResponseBody()
                    )
                )))),
            syncRemoteGreenCardsUseCase = fakeSyncRemoteGreenCardUseCase(
                result = SyncRemoteGreenCardsResult.Success
            ),
            removeExpiredEventsUseCase = fakeRemoveExpiredEventsUseCase(),
            persistenceManager = fakePersistenceManager()
        )

        val databaseSyncerResult = holderDatabaseSyncer.sync(
            expectedOriginType = OriginType.Test,
            syncWithRemote = true
        )

        assertTrue(databaseSyncerResult is DatabaseSyncerResult.Failed.ServerError)
    }

    @Test
    fun `sync returns Error if getting remote green cards returns Error`() = runBlocking {
        coEvery { eventGroupDao.getAll() } answers { events }
        coEvery { greenCardDao.getAll() } answers { listOf() }

        val holderDatabaseSyncer = HolderDatabaseSyncerImpl(
            holderDatabase = holderDatabase,
            greenCardUtil = fakeGreenCardUtil(),
            getRemoteGreenCardsUseCase = fakeGetRemoteGreenCardUseCase(
                result = RemoteGreenCardsResult.Error(NetworkRequestResult.Failed.Error(Step(1), IllegalStateException("Some error")))),
            syncRemoteGreenCardsUseCase = fakeSyncRemoteGreenCardUseCase(
                result = SyncRemoteGreenCardsResult.Success
            ),
            removeExpiredEventsUseCase = fakeRemoveExpiredEventsUseCase(),
            persistenceManager = fakePersistenceManager()
        )

        val databaseSyncerResult = holderDatabaseSyncer.sync(
            expectedOriginType = OriginType.Test,
            syncWithRemote = true
        )

        assertTrue(databaseSyncerResult is DatabaseSyncerResult.Failed.Error)
    }

    @Test
    fun `sync returns Error if syncing remote green cards failed`() = runBlocking {
        coEvery { eventGroupDao.getAll() } answers { events }

        val holderDatabaseSyncer = HolderDatabaseSyncerImpl(
            holderDatabase = holderDatabase,
            greenCardUtil = fakeGreenCardUtil(),
            getRemoteGreenCardsUseCase = fakeGetRemoteGreenCardUseCase(
                result = RemoteGreenCardsResult.Success(
                    remoteGreenCards = RemoteGreenCards(
                        domesticGreencard = RemoteGreenCards.DomesticGreenCard(
                            origins = listOf(RemoteGreenCards.Origin(
                                type = OriginType.Test,
                                eventTime = OffsetDateTime.now(),
                                expirationTime = OffsetDateTime.now(),
                                validFrom = OffsetDateTime.now()
                            )),
                            createCredentialMessages = "".toByteArray()
                        ),
                        euGreencards = null
                    )
                )
            ),
            syncRemoteGreenCardsUseCase = fakeSyncRemoteGreenCardUseCase(
                result = SyncRemoteGreenCardsResult.Failed(AppErrorResult(Step(1), IllegalStateException("Some error")))
            ),
            removeExpiredEventsUseCase = fakeRemoveExpiredEventsUseCase(),
            persistenceManager = fakePersistenceManager()
        )

        val databaseSyncerResult = holderDatabaseSyncer.sync(
            expectedOriginType = OriginType.Test,
            syncWithRemote = true
        )

        assertTrue(databaseSyncerResult is DatabaseSyncerResult.Failed.Error)
    }

    private fun successResult(originType: OriginType): RemoteGreenCardsResult = RemoteGreenCardsResult.Success(
        remoteGreenCards = RemoteGreenCards(
            domesticGreencard = RemoteGreenCards.DomesticGreenCard(
                origins = listOf(RemoteGreenCards.Origin(
                    type = originType,
                    eventTime = OffsetDateTime.now(),
                    expirationTime = OffsetDateTime.now(),
                    validFrom = OffsetDateTime.now()
                )),
                createCredentialMessages = "".toByteArray()
            ),
            euGreencards = null
        )
    )
}