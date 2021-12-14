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
    private val instructionsNavigateStateUseCase: InstructionsNavigateStateUseCase
): ScanInstructionsButtonUtil {

    override fun getButtonText(isFinalScreen: Boolean): Int {
        return if (isFinalScreen) {
            finalScreenCopy()
        } else {
            R.string.onboarding_next
        }
    }

    private fun finalScreenCopy(): Int {
        return when (val instructionsNavigationState = instructionsNavigateStateUseCase.get()) {
            is InstructionsNavigateState.Scanner -> if (instructionsNavigationState.isLocked) {
                R.string.verifier_scan_instructions_back_to_start
            } else {
                R.string.scan_qr_button
            }
            InstructionsNavigateState.VerificationPolicySelection -> R.string.onboarding_next
        }
    }
}