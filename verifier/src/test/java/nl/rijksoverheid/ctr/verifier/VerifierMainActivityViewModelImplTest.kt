package nl.rijksoverheid.ctr.verifier

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import nl.rijksoverheid.ctr.verifier.ui.policy.ConfigVerificationPolicyUseCase
import nl.rijksoverheid.ctr.verifier.ui.scanlog.usecase.ScanLogsCleanupUseCase
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class VerifierMainActivityViewModelImplTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        Dispatchers.setMain(TestCoroutineDispatcher())
    }

    @Test
    fun `cleanup calls cleanup usecases`() = runBlocking {
        val scanLogsCleanupUseCase = mockk<ScanLogsCleanupUseCase>()
        val viewModel = VerifierMainActivityViewModelImpl(scanLogsCleanupUseCase, mockk())

        viewModel.cleanup()

        coVerify { scanLogsCleanupUseCase.cleanup() }
    }

    @Test
    fun `on policy update should give policy update`() = runBlocking {
        val scanLogsCleanupUseCase = mockk<ScanLogsCleanupUseCase>()
        val configVerificationPolicyUseCase = mockk<ConfigVerificationPolicyUseCase> {
            coEvery { updatePolicy() } returns true
        }
        val viewModel = VerifierMainActivityViewModelImpl(
            scanLogsCleanupUseCase, configVerificationPolicyUseCase
        )

        viewModel.policyUpdate()

        assertEquals(true, viewModel.isPolicyUpdatedLiveData.value!!.peekContent())
    }
}