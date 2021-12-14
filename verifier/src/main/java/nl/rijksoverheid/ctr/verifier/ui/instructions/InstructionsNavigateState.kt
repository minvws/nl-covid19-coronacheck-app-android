package nl.rijksoverheid.ctr.verifier.ui.instructions

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
sealed class InstructionsNavigateState {
    data class Scanner(val isLocked: Boolean): InstructionsNavigateState()
    object VerificationPolicySelection: InstructionsNavigateState()
}
