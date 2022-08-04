package nl.rijksoverheid.ctr.verifier.policy

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class VerificationPolicySelectionViewModelImplTest : AutoCloseKoinTest() {

    private val verificationPolicyUseCase = mockk<VerificationPolicySelectionUseCase>().apply {
        coEvery { store(VerificationPolicy.VerificationPolicy3G) } returns Unit
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun `policy selected is stored`() = runBlocking {
        val viewModel = VerificationPolicySelectionViewModelImpl(verificationPolicyUseCase, mockk())

        viewModel.storeSelection(VerificationPolicy.VerificationPolicy3G)

        coVerify { verificationPolicyUseCase.store(VerificationPolicy.VerificationPolicy3G) }
    }

    @Test
    fun `policy selected is not stored`() = runBlocking {
        val viewModel = VerificationPolicySelectionViewModelImpl(verificationPolicyUseCase, mockk())

        viewModel.storeSelection(VerificationPolicy.VerificationPolicy3G)

        coVerify { verificationPolicyUseCase.store(VerificationPolicy.VerificationPolicy3G) }
    }
}
