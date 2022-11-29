package nl.rijksoverheid.ctr.holder.your_events

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import nl.rijksoverheid.ctr.getOrAwaitValue
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol
import nl.rijksoverheid.ctr.holder.your_events.models.ConflictingEventResult
import nl.rijksoverheid.ctr.holder.your_events.usecases.SaveEventsUseCase
import nl.rijksoverheid.ctr.persistence.database.HolderDatabaseSyncer
import nl.rijksoverheid.ctr.persistence.database.usecases.DraftEventUseCase
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class YourEventsViewModelImplTest {
    private val saveEventsUseCase: SaveEventsUseCase = mockk(relaxed = true)
    private val holderDatabaseSyncer: HolderDatabaseSyncer = mockk(relaxed = true)
    private val draftEventUseCase: DraftEventUseCase = mockk(relaxed = true)

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @Test
    fun `checkForConflictingEvents removes draft events and emits conflicting events if any`() {
        val yourEventsViewModel =
            YourEventsViewModelImpl(saveEventsUseCase, holderDatabaseSyncer, draftEventUseCase)
        val remoteProtocols: Map<RemoteProtocol, ByteArray> = mockk()
        coEvery { saveEventsUseCase.remoteProtocols3AreConflicting(remoteProtocols) } returns ConflictingEventResult.None

        yourEventsViewModel.checkForConflictingEvents(remoteProtocols)

        coVerify { draftEventUseCase.remove() }
        assertEquals(
            ConflictingEventResult.None,
            yourEventsViewModel.conflictingEventsResult.getOrAwaitValue().getContentIfNotHandled()
        )
    }
}
