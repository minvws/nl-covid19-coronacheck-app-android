package nl.rijksoverheid.ctr.holder.fuzzy_matching

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import nl.rijksoverheid.ctr.holder.dashboard.util.GreenCardUtil
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FuzzyMatchingOnboardingViewModelTest {
    private val holderDatabase: HolderDatabase = mockk()
    private val greenCardUtil: GreenCardUtil = mockk()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @Test
    fun `canGoBack from getEventsFlow is true`() {
        val viewModel = FuzzyMatchingOnboardingViewModel(holderDatabase, greenCardUtil)

        viewModel.canSkip(true)

        assertTrue(viewModel.toolbarButtonsStateLiveData.value!!.canGoBack)
    }

    @Test
    fun `canGoBack with active credential left is true`() {
        coEvery { greenCardUtil.hasNoActiveCredentials(any(), any()) } returns false
        coEvery { holderDatabase.greenCardDao().getAll() } returns listOf(mockk(relaxed = true))
        val viewModel = FuzzyMatchingOnboardingViewModel(holderDatabase, greenCardUtil)

        viewModel.canSkip(false)

        assertTrue(viewModel.toolbarButtonsStateLiveData.value!!.canGoBack)
    }

    @Test
    fun `canGoBack with no active credential left is false`() {
        coEvery { greenCardUtil.hasNoActiveCredentials(any(), any()) } returns true
        coEvery { holderDatabase.greenCardDao().getAll() } returns listOf(mockk(relaxed = true))
        val viewModel = FuzzyMatchingOnboardingViewModel(holderDatabase, greenCardUtil)

        viewModel.canSkip(false)

        assertFalse(viewModel.toolbarButtonsStateLiveData.value!!.canGoBack)
    }
}
