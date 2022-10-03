package nl.rijksoverheid.ctr.verifier

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.verifier.policy.ConfigVerificationPolicyUseCase
import nl.rijksoverheid.ctr.verifier.scanlog.usecase.ScanLogsCleanupUseCase
import org.junit.Before
import org.junit.Rule
import org.junit.Test

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
        val viewModel = VerifierMainActivityViewModelImpl(scanLogsCleanupUseCase, mockk(), mockk())

        viewModel.cleanup()

        coVerify { scanLogsCleanupUseCase.cleanup() }
    }

    @Test
    fun `on policy update should give policy update`() = runBlocking {
        val scanLogsCleanupUseCase = mockk<ScanLogsCleanupUseCase>()
        val configVerificationPolicyUseCase = mockk<ConfigVerificationPolicyUseCase> {
            coEvery { updatePolicy() } returns true
        }
        val cachedAppConfigUseCase = mockk<CachedAppConfigUseCase>() {
            coEvery { getCachedAppConfig().appDeactivated } returns false
        }
        val viewModel = VerifierMainActivityViewModelImpl(
            scanLogsCleanupUseCase, configVerificationPolicyUseCase, cachedAppConfigUseCase
        )

        viewModel.policyUpdate()

        assertEquals(true, viewModel.isPolicyUpdatedLiveData.value!!.peekContent())
    }
}
