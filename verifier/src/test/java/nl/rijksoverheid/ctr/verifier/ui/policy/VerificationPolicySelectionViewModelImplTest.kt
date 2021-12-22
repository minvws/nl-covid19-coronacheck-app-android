package nl.rijksoverheid.ctr.verifier.ui.policy

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import org.junit.Rule
import org.junit.Test

class VerificationPolicySelectionViewModelImplTest {

    private val verificationPolicyUseCase = mockk<VerificationPolicyUseCase>().apply {
        every { store(VerificationPolicy.VerificationPolicy3G) } returns Unit
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun `policy selected is stored`() {
        val viewModel = VerificationPolicySelectionViewModelImpl(verificationPolicyUseCase, mockk())

        viewModel.storeSelection(VerificationPolicy.VerificationPolicy3G)

        verify { verificationPolicyUseCase.store(VerificationPolicy.VerificationPolicy3G) }
    }
}
