package nl.rijksoverheid.ctr.verifier.ui.policy

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.*
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import org.junit.Rule
import org.junit.Test

class VerificationPolicySelectionViewModelImplTest {

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
}
