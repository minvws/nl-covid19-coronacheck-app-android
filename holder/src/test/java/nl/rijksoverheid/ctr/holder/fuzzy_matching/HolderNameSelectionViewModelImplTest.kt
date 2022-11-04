package nl.rijksoverheid.ctr.holder.fuzzy_matching

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import nl.rijksoverheid.ctr.getOrAwaitValue
import nl.rijksoverheid.ctr.holder.dashboard.util.GreenCardUtil
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol
import nl.rijksoverheid.ctr.holder.get_events.usecases.GetRemoteProtocolFromEventGroupUseCase
import nl.rijksoverheid.ctr.holder.your_events.utils.YourEventsFragmentUtil
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HolderNameSelectionViewModelImplTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val matchedEventsUseCase: MatchedEventsUseCase = mockk()
    private val getRemoteProtocolFromEventGroupUseCase: GetRemoteProtocolFromEventGroupUseCase =
        mockk()
    private val selectionDataUtil: SelectionDataUtil = mockk(relaxed = true)
    private val yourEventsFragmentUtil: YourEventsFragmentUtil = mockk()
    private val holderDatabase: HolderDatabase = mockk()
    private val greenCardUtil: GreenCardUtil = mockk()

    private val matchingBlobIds = listOf(listOf(3, 1), listOf(4, 2))

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @Test
    fun `given two groups with two events each, when init, then return two grouped list items`() =
        runTest {
            mockEvents()

            val viewModel = HolderNameSelectionViewModelImpl(
                matchedEventsUseCase,
                getRemoteProtocolFromEventGroupUseCase,
                selectionDataUtil,
                yourEventsFragmentUtil,
                holderDatabase,
                greenCardUtil,
                matchingBlobIds
            )

            val items = viewModel.itemsLiveData.getOrAwaitValue()

            assertEquals("firstNameA1", (items[1] as HolderNameSelectionItem.ListItem).name)
            assertEquals("firstNameA2", (items[2] as HolderNameSelectionItem.ListItem).name)
        }

    @Test
    fun `given two groups with two events each, when first group selected, then its name is selected`() =
        runTest {
            mockEvents()

            val viewModel = HolderNameSelectionViewModelImpl(
                matchedEventsUseCase,
                getRemoteProtocolFromEventGroupUseCase,
                selectionDataUtil,
                yourEventsFragmentUtil,
                holderDatabase,
                greenCardUtil,
                matchingBlobIds
            )
            viewModel.onItemSelected("firstNameA2")

            val items = viewModel.itemsLiveData.getOrAwaitValue()

            assertEquals(false, (items[1] as HolderNameSelectionItem.ListItem).isSelected)
            assertEquals(true, (items[1] as HolderNameSelectionItem.ListItem).willBeRemoved)
            assertEquals(false, (items[1] as HolderNameSelectionItem.ListItem).nothingSelectedError)
            assertEquals(true, (items[2] as HolderNameSelectionItem.ListItem).isSelected)
            assertEquals(false, (items[2] as HolderNameSelectionItem.ListItem).willBeRemoved)
            assertEquals(false, (items[2] as HolderNameSelectionItem.ListItem).nothingSelectedError)
        }

    @Test
    fun `given two groups with two events each, when nothing selected, then nothing is selected error`() =
        runTest {
            mockEvents()

            val viewModel = HolderNameSelectionViewModelImpl(
                matchedEventsUseCase,
                getRemoteProtocolFromEventGroupUseCase,
                selectionDataUtil,
                yourEventsFragmentUtil,
                holderDatabase,
                greenCardUtil,
                matchingBlobIds
            )
            viewModel.nothingSelectedError()

            val items = viewModel.itemsLiveData.getOrAwaitValue()

            assertEquals(false, (items[1] as HolderNameSelectionItem.ListItem).isSelected)
            assertEquals(false, (items[1] as HolderNameSelectionItem.ListItem).willBeRemoved)
            assertEquals(true, (items[1] as HolderNameSelectionItem.ListItem).nothingSelectedError)
            assertEquals(false, (items[2] as HolderNameSelectionItem.ListItem).isSelected)
            assertEquals(false, (items[2] as HolderNameSelectionItem.ListItem).willBeRemoved)
            assertEquals(true, (items[2] as HolderNameSelectionItem.ListItem).nothingSelectedError)
        }

    @Test
    fun `given two groups with two events each, when store first item selected, then first item is stored`() =
        runTest {
            mockEvents()
            val itemSlot = slot<Int>()
            coEvery { matchedEventsUseCase.selected(any(), matchingBlobIds) } just runs

            val viewModel = HolderNameSelectionViewModelImpl(
                matchedEventsUseCase,
                getRemoteProtocolFromEventGroupUseCase,
                selectionDataUtil,
                yourEventsFragmentUtil,
                holderDatabase,
                greenCardUtil,
                matchingBlobIds
            )
            viewModel.onItemSelected("firstNameA1")
            viewModel.storeSelection {}

            coVerify { matchedEventsUseCase.selected(capture(itemSlot), matchingBlobIds) }
            assertEquals(0, itemSlot.captured)
        }

    @Test
    fun `given no name is selected, when store selection, then nothing selected error`() = runTest {
        mockEvents()

        val viewModel = HolderNameSelectionViewModelImpl(
            matchedEventsUseCase,
            getRemoteProtocolFromEventGroupUseCase,
            selectionDataUtil,
            yourEventsFragmentUtil,
            holderDatabase,
            greenCardUtil,
            matchingBlobIds
        )

        viewModel.storeSelection { }

        assertTrue(viewModel.nameSelectionError.getOrAwaitValue())
    }

    @Test
    fun `given name no name is selected yet, when select name and store selection, then no error`() =
        runTest {
            mockEvents()
            coEvery { matchedEventsUseCase.selected(any(), any()) } just runs

            val viewModel = HolderNameSelectionViewModelImpl(
                matchedEventsUseCase,
                getRemoteProtocolFromEventGroupUseCase,
                selectionDataUtil,
                yourEventsFragmentUtil,
                holderDatabase,
                greenCardUtil,
                matchingBlobIds
            )

            viewModel.onItemSelected("firstNameA1")
            viewModel.storeSelection {
                assertEquals("firstNameA1", it)
            }

            assertFalse(viewModel.nameSelectionError.getOrAwaitValue())
        }

    private suspend fun mockEvents() {
        coEvery { holderDatabase.eventGroupDao().getAll() } returns getEvents()
        val eventSlot = slot<EventGroupEntity>()
        coEvery { getRemoteProtocolFromEventGroupUseCase.get(capture(eventSlot)) } answers {
            getRemoteProtocolFromEventGroupEntity(
                eventSlot.captured.apply { eventSlot.clear() })
        }
        val nameSlot = slot<RemoteProtocol.Holder>()
        coEvery { yourEventsFragmentUtil.getFullName(capture(nameSlot)) } answers {
            nameSlot.captured.apply { nameSlot.clear() }.firstName!!
        }
    }

    private fun getEvents(): List<EventGroupEntity> {
        return listOf(
            mockk<EventGroupEntity>().apply { coEvery { id } returns 1 },
            mockk<EventGroupEntity>().apply { coEvery { id } returns 2 },
            mockk<EventGroupEntity>().apply { coEvery { id } returns 3 },
            mockk<EventGroupEntity>().apply { coEvery { id } returns 4 }
        )
    }

    private fun getRemoteProtocolFromEventGroupEntity(event: EventGroupEntity): RemoteProtocol {
        return mockk<RemoteProtocol>().apply {
            coEvery { providerIdentifier } returns "dcc"
            coEvery { events } returns emptyList()
            coEvery { holder } returns mockk<RemoteProtocol.Holder>().apply {
                coEvery { firstName } returns "firstNameA${
                    if (event.id < 3) {
                        event.id
                    } else {
                        ""
                    }
                }"
            }
        }
    }
}
