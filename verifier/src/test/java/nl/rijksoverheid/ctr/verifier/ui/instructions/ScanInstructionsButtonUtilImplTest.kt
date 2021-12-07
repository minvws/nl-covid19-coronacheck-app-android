package nl.rijksoverheid.ctr.verifier.ui.instructions

import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.ui.scanqr.ScannerNavigationState
import nl.rijksoverheid.ctr.verifier.ui.scanqr.ScannerNavigationStateUseCase
import org.junit.Assert
import org.junit.Test

class ScanInstructionsButtonUtilImplTest {

    @Test
    fun `getButtonText returns correct text when last screen and scanner locked`() {
        val scannerNavigationStateUseCase = mockk<ScannerNavigationStateUseCase>()
        every { scannerNavigationStateUseCase.get() } answers { ScannerNavigationState.Scanner(isLocked = true) }
        val util = ScanInstructionsButtonUtilImpl(scannerNavigationStateUseCase)

        val buttonText = util.getButtonText(true)
        Assert.assertEquals(buttonText, R.string.verifier_scan_instructions_back_to_start)
    }

    @Test
    fun `getButtonText returns correct text when last screen and scanner not locked`() {
        val scannerNavigationStateUseCase = mockk<ScannerNavigationStateUseCase>()
        every { scannerNavigationStateUseCase.get() } answers { ScannerNavigationState.Scanner(isLocked = false) }
        val util = ScanInstructionsButtonUtilImpl(scannerNavigationStateUseCase)

        val buttonText = util.getButtonText(true)
        Assert.assertEquals(buttonText, R.string.scan_qr_button)
    }

    @Test
    fun `getButtonText returns correct text when last screen and next screen is not scanner`() {
        val scannerNavigationStateUseCase = mockk<ScannerNavigationStateUseCase>()
        every { scannerNavigationStateUseCase.get() } answers { ScannerNavigationState.Instructions }
        val util = ScanInstructionsButtonUtilImpl(scannerNavigationStateUseCase)

        val buttonText = util.getButtonText(true)
        Assert.assertEquals(buttonText, R.string.onboarding_next)
    }

    @Test
    fun `getButtonText returns correct text when not last screen`() {
        val scannerNavigationStateUseCase = mockk<ScannerNavigationStateUseCase>()
        every { scannerNavigationStateUseCase.get() } answers { ScannerNavigationState.Scanner(isLocked = false) }
        val util = ScanInstructionsButtonUtilImpl(scannerNavigationStateUseCase)

        val buttonText = util.getButtonText(false)
        Assert.assertEquals(buttonText, R.string.onboarding_next)
    }
}