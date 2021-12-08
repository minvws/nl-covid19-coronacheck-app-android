package nl.rijksoverheid.ctr.verifier.ui.policy

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.*
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import nl.rijksoverheid.ctr.verifier.models.ScannerState
import nl.rijksoverheid.ctr.verifier.usecase.ScannerStateUseCase
import org.junit.Assert.*
import org.junit.Rule

import org.junit.Test

class VerificationPolicySelectionViewModelImplTest {

    private val verificationPolicyUseCase = mockk<VerificationPolicyUseCase>().apply {
        every { store(VerificationPolicy.VerificationPolicy3G) } returns Unit
    }

    private val scannerStateUseCase = mockk<ScannerStateUseCase>()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun `init on scanQR flow with right state`() {
        val viewModel = VerificationPolicySelectionViewModelImpl(verificationPolicyUseCase, scannerStateUseCase)

        viewModel.init(VerificationPolicyFlow.FirstTimeUse(ScannerState.Unlocked(VerificationPolicyState.Policy2G)))

        assertEquals(VerificationPolicyState.Policy2G, (viewModel.policyFlowLiveData.value as VerificationPolicyFlow.FirstTimeUse).state)
    }

    @Test
    fun `init on settings flow with right state`() {
        val viewModel = VerificationPolicySelectionViewModelImpl(verificationPolicyUseCase, scannerStateUseCase)

        viewModel.init(VerificationPolicyFlow.Info(ScannerState.Unlocked(VerificationPolicyState.Policy2G)))

        assertEquals(VerificationPolicyState.Policy2G, (viewModel.policyFlowLiveData.value as VerificationPolicyFlow.Info).state)
    }

    @Test
    fun `policy selected is stored`() {
        val viewModel = VerificationPolicySelectionViewModelImpl(verificationPolicyUseCase, scannerStateUseCase)

        viewModel.storeSelection(VerificationPolicy.VerificationPolicy3G)

        verify { verificationPolicyUseCase.store(VerificationPolicy.VerificationPolicy3G) }
    }
}
