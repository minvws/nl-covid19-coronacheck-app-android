package nl.rijksoverheid.ctr.verifier.ui.scanlog

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.verifier.ui.scanlog.items.ScanLogItem
import nl.rijksoverheid.ctr.verifier.ui.scanlog.usecase.GetScanLogItemsUseCase
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ScanLogViewModelImplTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        Dispatchers.setMain(TestCoroutineDispatcher())
    }

    @Test
    fun `getItems() delegates result to livedata()`() = runBlocking {
        val scanLogItems = listOf(
            ScanLogItem.HeaderItem(0L)
        )

        val getScanLogItemsUseCase = mockk<GetScanLogItemsUseCase>()
        coEvery { getScanLogItemsUseCase.getItems() } answers { scanLogItems }

        val viewModel = ScanLogViewModelImpl(
            getScanLogItemsUseCase = getScanLogItemsUseCase
        )

        viewModel.getItems()

        assertEquals(viewModel.scanLogItemsLiveData.value, scanLogItems)
    }
}