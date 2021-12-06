package nl.rijksoverheid.ctr.verifier.ui.policy

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
sealed class VerificationPolicyFlow(val state: VerificationPolicyState) {
    class ScanQR(policyState: VerificationPolicyState) : VerificationPolicyFlow(policyState)
    class Settings(policyState: VerificationPolicyState) : VerificationPolicyFlow(policyState)
}
