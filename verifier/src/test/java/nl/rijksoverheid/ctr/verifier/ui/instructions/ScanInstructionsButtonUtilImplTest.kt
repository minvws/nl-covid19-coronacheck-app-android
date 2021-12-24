package nl.rijksoverheid.ctr.verifier.ui.instructions

import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.verifier.R
import org.junit.Assert.*
import org.junit.Test

class ScanInstructionsButtonUtilImplTest {

    @Test
    fun `getButtonText returns correct text when last screen and scanner locked`() {
        val instructionsNavigateStateUseCase = mockk<InstructionsNavigateStateUseCase>()
        every { instructionsNavigateStateUseCase.get() } answers { InstructionsNavigateState.Scanner(isLocked = true) }
        val util = ScanInstructionsButtonUtilImpl(instructionsNavigateStateUseCase)

        val buttonText = util.getButtonText(true)
        assertEquals(buttonText, R.string.verifier_scan_instructions_back_to_start)
    }

    @Test
    fun `getButtonText returns correct text when last screen and scanner not locked`() {
        val instructionsNavigateStateUseCase = mockk<InstructionsNavigateStateUseCase>()
        every { instructionsNavigateStateUseCase.get() } answers { InstructionsNavigateState.Scanner(isLocked = false) }
        val util = ScanInstructionsButtonUtilImpl(instructionsNavigateStateUseCase)

        val buttonText = util.getButtonText(true)
        assertEquals(buttonText, R.string.scan_qr_button)
    }

    @Test
    fun `getButtonText returns correct text when not last screen`() {
        val instructionsNavigateStateUseCase = mockk<InstructionsNavigateStateUseCase>()
        every { instructionsNavigateStateUseCase.get() } answers { InstructionsNavigateState.Scanner(isLocked = false) }
        val util = ScanInstructionsButtonUtilImpl(instructionsNavigateStateUseCase)

        val buttonText = util.getButtonText(false)
        assertEquals(buttonText, R.string.onboarding_next)
    }

    @Test
    fun `getButtonText returns onboarding_next when last screen and policy not set yet`() {
        val instructionsNavigateStateUseCase = mockk<InstructionsNavigateStateUseCase>()
        every { instructionsNavigateStateUseCase.get() } answers { InstructionsNavigateState.VerificationPolicySelection }
        val util = ScanInstructionsButtonUtilImpl(instructionsNavigateStateUseCase)

        val buttonText = util.getButtonText(true)
        assertEquals(buttonText, R.string.onboarding_next)
    }
}