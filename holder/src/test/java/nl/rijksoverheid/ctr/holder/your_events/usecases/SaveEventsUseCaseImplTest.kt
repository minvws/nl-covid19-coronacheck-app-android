package nl.rijksoverheid.ctr.holder.your_events.usecases

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccination
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol
import nl.rijksoverheid.ctr.holder.get_events.utils.ScopeUtil
import nl.rijksoverheid.ctr.holder.your_events.models.ConflictingEventResult
import nl.rijksoverheid.ctr.holder.your_events.utils.RemoteEventHolderUtil
import nl.rijksoverheid.ctr.holder.your_events.utils.RemoteEventUtil
import nl.rijksoverheid.ctr.holder.your_events.utils.RemoteProtocol3Util
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class SaveEventsUseCaseImplTest {
    private val holderDatabase: HolderDatabase = mockk()
    private val remoteEventHolderUtil: RemoteEventHolderUtil = mockk()
    private val scopeUtil: ScopeUtil = mockk()
    private val remoteEventUtil: RemoteEventUtil = mockk()
    private val remoteProtocol3Util: RemoteProtocol3Util = mockk()

    private val holder = RemoteProtocol.Holder(
        infix = "", firstName = "Onoma", lastName = "Epitheto", birthDate = "01-08-1990"
    )
    private val remoteProtocols = mapOf(
        RemoteProtocol(
            providerIdentifier = "ZZZ",
            protocolVersion = "3.0",
            status = RemoteProtocol.Status.COMPLETE,
            holder = holder,
            events = listOf(
                RemoteEventVaccination(
                    type = "vaccination",
                    unique = "ZZZ",
                    vaccination = mockk()
                )
            )
        ) to "".toByteArray()
    )

    @Test
    fun `remoteProtocols3AreConflicting returns Existing for dcc events`() = runTest {
        coEvery { remoteEventUtil.isDccEvent(any()) } returns true
        coEvery {
            holderDatabase.eventGroupDao().getAll()
        } returns listOf(mockk<EventGroupEntity>().apply {
            coEvery { providerIdentifier } returns "ZZZ"
        })
        val saveEventsUseCase = SaveEventsUseCaseImpl(
            holderDatabase,
            remoteEventHolderUtil,
            scopeUtil,
            remoteEventUtil,
            remoteProtocol3Util
        )

        val actual = saveEventsUseCase.remoteProtocols3AreConflicting(remoteProtocols)

        assertEquals(ConflictingEventResult.Existing, actual)
    }

    @Test
    fun `remoteProtocols3AreConflicting returns None for non dcc events`() = runTest {
        coEvery { remoteEventUtil.isDccEvent(any()) } returns false
        coEvery {
            holderDatabase.eventGroupDao().getAll()
        } returns listOf(mockk<EventGroupEntity>().apply {
            coEvery { providerIdentifier } returns "ZZZ"
            coEvery { jsonData } returns "".toByteArray()
        })
        coEvery { remoteEventHolderUtil.holder(any(), any()) } returns holder
        coEvery { remoteEventHolderUtil.conflicting(any(), any()) } returns false
        val saveEventsUseCase = SaveEventsUseCaseImpl(
            holderDatabase,
            remoteEventHolderUtil,
            scopeUtil,
            remoteEventUtil,
            remoteProtocol3Util
        )

        val actual = saveEventsUseCase.remoteProtocols3AreConflicting(remoteProtocols)

        assertEquals(ConflictingEventResult.None, actual)
    }
}
