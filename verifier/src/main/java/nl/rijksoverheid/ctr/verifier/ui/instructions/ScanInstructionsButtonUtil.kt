package nl.rijksoverheid.ctr.verifier.ui.instructions

import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.ui.scanqr.ScannerNavigationState
import nl.rijksoverheid.ctr.verifier.ui.scanqr.ScannerNavigationStateUseCase
import timber.log.Timber

interface ScanInstructionsButtonUtil {
    fun getButtonText(isFinalScreen: Boolean): Int
}

class ScanInstructionsButtonUtilImpl(
    private val scannerNavigationStateUseCase: ScannerNavigationStateUseCase
): ScanInstructionsButtonUtil {

    override fun getButtonText(isFinalScreen: Boolean): Int {
        val scannerNavigationState = scannerNavigationStateUseCase.get()
        return if (isFinalScreen) {
            if (scannerNavigationState == ScannerNavigationState.Scanner) {
                R.string.scan_qr_button
            } else {
                R.string.onboarding_next
            }
        } else {
            R.string.onboarding_next
        }
    }
}