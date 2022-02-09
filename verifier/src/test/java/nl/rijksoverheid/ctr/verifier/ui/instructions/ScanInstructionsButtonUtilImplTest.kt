package nl.rijksoverheid.ctr.verifier.ui.instructions

import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.ui.scanqr.ScannerNavigationState.*
import nl.rijksoverheid.ctr.verifier.ui.scanqr.ScannerNavigationStateUseCase
import org.junit.Assert.*
import org.junit.Test

class ScanInstructionsButtonUtilImplTest {

    @Test
    fun `getButtonText returns correct text when last screen and scanner locked`() {
        val instructionsNavigateStateUseCase = mockk<ScannerNavigationStateUseCase>()
        every { instructionsNavigateStateUseCase.get() } answers { Scanner(isLocked = true) }
        val util = ScanInstructionsButtonUtilImpl(instructionsNavigateStateUseCase)

        val buttonText = util.getButtonText(true)
        assertEquals(buttonText, R.string.verifier_scan_instructions_back_to_start)
    }

    @Test
    fun `getButtonText returns correct text when last screen and scanner not locked`() {
        val instructionsNavigateStateUseCase = mockk<ScannerNavigationStateUseCase>()
        every { instructionsNavigateStateUseCase.get() } answers { Scanner(isLocked = false) }
        val util = ScanInstructionsButtonUtilImpl(instructionsNavigateStateUseCase)

        val buttonText = util.getButtonText(true)
        assertEquals(buttonText, R.string.scan_qr_button)
    }

    @Test
    fun `getButtonText returns correct text when not last screen`() {
        val instructionsNavigateStateUseCase = mockk<ScannerNavigationStateUseCase>()
        every { instructionsNavigateStateUseCase.get() } answers { Scanner(isLocked = false) }
        val util = ScanInstructionsButtonUtilImpl(instructionsNavigateStateUseCase)

        val buttonText = util.getButtonText(false)
        assertEquals(buttonText, R.string.onboarding_next)
    }

    @Test
    fun `getButtonText returns onboarding_next when last screen and policy not set yet`() {
        val instructionsNavigateStateUseCase = mockk<ScannerNavigationStateUseCase>()
        every { instructionsNavigateStateUseCase.get() } answers { VerificationPolicySelection }
        val util = ScanInstructionsButtonUtilImpl(instructionsNavigateStateUseCase)

        val buttonText = util.getButtonText(true)
        assertEquals(buttonText, R.string.onboarding_next)
    }

    @Test
    fun `getButtonText returns next when last screen and new policy rules are applied`() {
        val instructionsNavigateStateUseCase = mockk<ScannerNavigationStateUseCase>()
        every { instructionsNavigateStateUseCase.get() } answers { NewPolicyRules }
        val util = ScanInstructionsButtonUtilImpl(instructionsNavigateStateUseCase)

        val buttonText = util.getButtonText(true)
        assertEquals(buttonText, R.string.onboarding_next)
    }
}