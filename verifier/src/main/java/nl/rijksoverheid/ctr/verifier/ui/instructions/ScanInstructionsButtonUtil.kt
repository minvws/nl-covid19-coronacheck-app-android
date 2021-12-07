package nl.rijksoverheid.ctr.verifier.ui.instructions

import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.ui.scanqr.ScannerNavigationState
import nl.rijksoverheid.ctr.verifier.ui.scanqr.ScannerNavigationStateUseCase

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface ScanInstructionsButtonUtil {
    fun getButtonText(isFinalScreen: Boolean): Int
}

class ScanInstructionsButtonUtilImpl(
    private val scannerNavigationStateUseCase: ScannerNavigationStateUseCase
): ScanInstructionsButtonUtil {

    override fun getButtonText(isFinalScreen: Boolean): Int {
        val scannerNavigationState = scannerNavigationStateUseCase.get()
        return if (isFinalScreen) {
            if (scannerNavigationState is ScannerNavigationState.Scanner) {
                if (scannerNavigationState.isLocked) {
                    R.string.verifier_scan_instructions_back_to_start
                } else {
                    R.string.scan_qr_button
                }
            } else {
                R.string.onboarding_next
            }
        } else {
            R.string.onboarding_next
        }
    }
}