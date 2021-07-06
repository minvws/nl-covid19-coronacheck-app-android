package nl.rijksoverheid.ctr.holder.ui.myoverview

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabaseSyncer
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.usecases.GreenCardsUseCase
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.GetMyOverviewItemsUseCase
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.MyOverviewItem
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.MyOverviewItems
import nl.rijksoverheid.ctr.holder.ui.myoverview.items.GreenCardErrorState
import nl.rijksoverheid.ctr.shared.livedata.Event
import nl.rijksoverheid.ctr.shared.utils.AndroidUtil
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class MyOverviewViewModelTest {
    private val getMyOverviewItemsUseCase: GetMyOverviewItemsUseCase = mockk(relaxed = true)
    private val holderDatabaseSyncer: HolderDatabaseSyncer = mockk(relaxed = true)
    private val persistenceManager: PersistenceManager = mockk(relaxed = true)
    private val greenCardsUseCase: GreenCardsUseCase = mockk(relaxed = true)
    private val androidUtil: AndroidUtil = mockk(relaxed = true)

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        Dispatchers.setMain(TestCoroutineDispatcher())
    }

    @Test
    fun `get selectedType from the event if present`() {
        val myOverviewViewModel = MyOverviewViewModelImpl(getMyOverviewItemsUseCase, holderDatabaseSyncer, persistenceManager, greenCardsUseCase, androidUtil)
        (myOverviewViewModel.myOverviewItemsLiveData as MutableLiveData).postValue(Event(
            MyOverviewItems(emptyList(), GreenCardType.Domestic)
        ))

        val actualType = myOverviewViewModel.getSelectedType()

        assertEquals(GreenCardType.Domestic, actualType)
    }

    @Test
    fun `get selectedType from persistentManager is the event is empty`() {
        val myOverviewViewModel = MyOverviewViewModelImpl(getMyOverviewItemsUseCase, holderDatabaseSyncer, persistenceManager, greenCardsUseCase, androidUtil)
        every { persistenceManager.getSelectedGreenCardType() } returns GreenCardType.Domestic

        val actualType = myOverviewViewModel.getSelectedType()

        assertEquals(GreenCardType.Domestic, actualType)
    }

    @Test
    fun `apply bug fix if conditions apply and trigger dialog if sync fails`() = runBlockingTest {
        coEvery { androidUtil.isFirstInstall() } returns false
        coEvery { persistenceManager.hasAppliedJune28Fix() } returns false
        coEvery { greenCardsUseCase.faultyVaccinationsJune28() } returns true
        coEvery { holderDatabaseSyncer.sync(syncWithRemote = true) } returns DatabaseSyncerResult.NetworkError
        val myOverviewViewModel = MyOverviewViewModelImpl(getMyOverviewItemsUseCase, holderDatabaseSyncer, persistenceManager, greenCardsUseCase, androidUtil)

        myOverviewViewModel.refreshOverviewItems(GreenCardType.Domestic, true)

        assertEquals(MyOverviewError.Refresh, myOverviewViewModel.myOverviewRefreshErrorEvent.value?.peekContent())
        verify(exactly = 0) { persistenceManager.setJune28FixApplied(any()) }
    }

    @Test
    fun `apply bug fix if conditions apply and sync succeeded`() = runBlockingTest {
        coEvery { androidUtil.isFirstInstall() } returns false
        coEvery { persistenceManager.hasAppliedJune28Fix() } returns false
        coEvery { greenCardsUseCase.faultyVaccinationsJune28() } returns true
        coEvery { holderDatabaseSyncer.sync(syncWithRemote = true) } returns DatabaseSyncerResult.Success
        val myOverviewViewModel = MyOverviewViewModelImpl(getMyOverviewItemsUseCase, holderDatabaseSyncer, persistenceManager, greenCardsUseCase, androidUtil)

        myOverviewViewModel.refreshOverviewItems(GreenCardType.Domestic, true)

        assertEquals(MyOverviewError.Forced, myOverviewViewModel.myOverviewRefreshErrorEvent.value?.peekContent())
        verify(exactly = 1) { persistenceManager.setJune28FixApplied(true) }
    }

    @Test
    fun `sync if cards are expiring`() = runBlockingTest {
        coEvery { androidUtil.isFirstInstall() } returns false
        coEvery { persistenceManager.hasAppliedJune28Fix() } returns false
        coEvery { greenCardsUseCase.faultyVaccinationsJune28() } returns false
        coEvery { holderDatabaseSyncer.sync(syncWithRemote = true) } returns DatabaseSyncerResult.Success
        coEvery { greenCardsUseCase.expiring() } returns true
        val greenCardItems = MyOverviewItems(
            selectedType = GreenCardType.Domestic,
            items = emptyList()
        )
        coEvery { getMyOverviewItemsUseCase.get(1, GreenCardType.Domestic, loading = true) } returns loadingGreenCardItems()
        coEvery { getMyOverviewItemsUseCase.get(1, GreenCardType.Domestic) } returns greenCardItems
        val myOverviewViewModel = MyOverviewViewModelImpl(getMyOverviewItemsUseCase, holderDatabaseSyncer, persistenceManager, greenCardsUseCase, androidUtil)

        myOverviewViewModel.refreshOverviewItems(GreenCardType.Domestic, true)

        assertEquals(greenCardItems, myOverviewViewModel.myOverviewItemsLiveData.value?.peekContent())
    }

    @Test
    fun `trigger network error if sync fails due to network error`() = runBlockingTest {
        coEvery { androidUtil.isFirstInstall() } returns false
        coEvery { persistenceManager.hasAppliedJune28Fix() } returns false
        coEvery { greenCardsUseCase.faultyVaccinationsJune28() } returns false
        coEvery { holderDatabaseSyncer.sync(syncWithRemote = true) } returns DatabaseSyncerResult.NetworkError
        coEvery { greenCardsUseCase.expiring() } returns true
        val greenCardItems = MyOverviewItems(
            selectedType = GreenCardType.Domestic,
            items = listOf(MyOverviewItem.GreenCardItem(
                credentialState = mockk(),
                greenCard = mockk(),
                originStates = listOf(mockk()),
                loading = false,
                errorState = GreenCardErrorState.NetworkError,
            ))
        )
        coEvery { getMyOverviewItemsUseCase.get(1, GreenCardType.Domestic, loading = true) } returns loadingGreenCardItems()
        coEvery { getMyOverviewItemsUseCase.get(1, GreenCardType.Domestic, errorState = GreenCardErrorState.NetworkError) } returns greenCardItems
        coEvery { greenCardsUseCase.expiredCard(GreenCardType.Domestic) } returns true
        val myOverviewViewModel = MyOverviewViewModelImpl(getMyOverviewItemsUseCase, holderDatabaseSyncer, persistenceManager, greenCardsUseCase, androidUtil)

        myOverviewViewModel.refreshOverviewItems(GreenCardType.Domestic, true)

        assertEquals(MyOverviewError.Inactive, myOverviewViewModel.myOverviewRefreshErrorEvent.value?.peekContent())
    }

    @Test
    fun `trigger server error if sync fails due to server error`() = runBlockingTest {
        coEvery { androidUtil.isFirstInstall() } returns false
        coEvery { persistenceManager.hasAppliedJune28Fix() } returns false
        coEvery { greenCardsUseCase.faultyVaccinationsJune28() } returns false
        coEvery { holderDatabaseSyncer.sync(syncWithRemote = true) } returns DatabaseSyncerResult.ServerError(500)
        coEvery { greenCardsUseCase.expiring() } returns true
        val greenCardItems = MyOverviewItems(
            selectedType = GreenCardType.Domestic,
            items = listOf(MyOverviewItem.GreenCardItem(
                credentialState = mockk(),
                greenCard = mockk(),
                originStates = listOf(mockk()),
                loading = false,
                errorState = GreenCardErrorState.ServerError,
            ))
        )
        coEvery { getMyOverviewItemsUseCase.get(walletId = 1, selectedType = GreenCardType.Domestic, loading = true) } returns loadingGreenCardItems()
        coEvery { getMyOverviewItemsUseCase.get(walletId = 1, selectedType = GreenCardType.Domestic, errorState = GreenCardErrorState.ServerError) } returns greenCardItems
        coEvery { greenCardsUseCase.expiredCard(GreenCardType.Domestic) } returns true
        val myOverviewViewModel = MyOverviewViewModelImpl(getMyOverviewItemsUseCase, holderDatabaseSyncer, persistenceManager, greenCardsUseCase, androidUtil)

        myOverviewViewModel.refreshOverviewItems(GreenCardType.Domestic, true)

        assertTrue((myOverviewViewModel.myOverviewItemsLiveData.value?.peekContent()?.items?.first() as MyOverviewItem.GreenCardItem).errorState is GreenCardErrorState.ServerError)
        assertNull(myOverviewViewModel.myOverviewRefreshErrorEvent.value?.peekContent())
    }

    private fun loadingGreenCardItems() = MyOverviewItems(
        selectedType = GreenCardType.Domestic,
        items = listOf(MyOverviewItem.GreenCardItem(
            credentialState = mockk(),
            greenCard = mockk(),
            originStates = listOf(mockk()),
            loading = true,
            errorState = GreenCardErrorState.None,
        ))
    )

    @Test
    fun `don't sync if cards are not expiring`() {
        coEvery { androidUtil.isFirstInstall() } returns false
        coEvery { persistenceManager.hasAppliedJune28Fix() } returns false
        coEvery { greenCardsUseCase.faultyVaccinationsJune28() } returns false
        coEvery { holderDatabaseSyncer.sync(syncWithRemote = true) } returns DatabaseSyncerResult.NetworkError
        coEvery { greenCardsUseCase.expiring() } returns false
        val myOverviewViewModel = MyOverviewViewModelImpl(getMyOverviewItemsUseCase, holderDatabaseSyncer, persistenceManager, greenCardsUseCase, androidUtil)

        myOverviewViewModel.refreshOverviewItems(GreenCardType.Domestic, true)

        coVerify(exactly = 1) { holderDatabaseSyncer.sync(
            syncWithRemote = false
        ) }
    }
}