package nl.rijksoverheid.ctr.verifier.ui.policy

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.*
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import org.junit.Assert.*
import org.junit.Rule

import org.junit.Test

class VerificationPolicySelectionViewModelImplTest {

    private val verificationPolicyUseCase = mockk<VerificationPolicyUseCase>().apply {
        every { get() } returns VerificationPolicyState.Policy2G
        every { store(VerificationPolicy.VerificationPolicy3G) } returns Unit
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun `init on scanQR flow with right state`() {
        val viewModel = VerificationPolicySelectionViewModelImpl(verificationPolicyUseCase, true)

        assertEquals(VerificationPolicyState.Policy2G, (viewModel.liveData.value as VerificationPolicyFlow.ScanQR).state)
    }

    @Test
    fun `init on settings flow with right state`() {
        val viewModel = VerificationPolicySelectionViewModelImpl(verificationPolicyUseCase, false)

        assertEquals(VerificationPolicyState.Policy2G, (viewModel.liveData.value as VerificationPolicyFlow.Settings).state)
    }

    @Test
    fun `policy selected is stored`() {
        val viewModel = VerificationPolicySelectionViewModelImpl(verificationPolicyUseCase, true)

        viewModel.storeSelection(VerificationPolicy.VerificationPolicy3G)

        verify { verificationPolicyUseCase.store(VerificationPolicy.VerificationPolicy3G) }
    }
}
