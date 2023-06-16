package nl.rijksoverheid.ctr.holder.dashboard

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardSync
import nl.rijksoverheid.ctr.holder.usecases.HolderFeatureFlagUseCase
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.persistence.database.usecases.DraftEventUseCase
import nl.rijksoverheid.ctr.persistence.database.usecases.RemoveExpiredEventsUseCase
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DashboardViewModelImplTest {
    private val holderDatabase: HolderDatabase = mockk(relaxed = true)
    private val removeExpiredEventsUseCase: RemoveExpiredEventsUseCase = mockk(relaxed = true)
    private val draftEventUseCase: DraftEventUseCase = mockk(relaxed = true)
    private val dashboardViewModel = DashboardViewModelImpl(
        mockk(relaxed = true),
        mockk(relaxed = true),
        mockk(relaxed = true) {
            coEvery { getItems(any(), any(), any(), any()) } returns mockk(relaxed = true)
        },
        mockk(relaxed = true),
        mockk(relaxed = true),
        mockk(relaxed = true),
        mockk(relaxed = true),
        mockk(relaxed = true) {
            coEvery { transform(any()) } returns mockk(relaxed = true)
        },
        removeExpiredEventsUseCase,
        draftEventUseCase,
        mockk<HolderFeatureFlagUseCase>().apply {
            every { isInArchiveMode() } returns false
        },
        mockk(relaxed = true)
    )

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @Test
    fun `on app resume refresh draft and expired events are removed`() {
        val events = mockk<List<EventGroupEntity>>()
        coEvery { holderDatabase.eventGroupDao().getAll() } returns events

        dashboardViewModel.refresh(DashboardSync.ForceSync)

        coVerify { draftEventUseCase.remove() }
        coVerify { removeExpiredEventsUseCase.execute(any()) }
    }
}
