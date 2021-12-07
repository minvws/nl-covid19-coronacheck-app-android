package nl.rijksoverheid.ctr.verifier.ui.scanqr

import nl.rijksoverheid.ctr.verifier.ui.policy.VerificationPolicyState
import nl.rijksoverheid.ctr.verifier.ui.policy.VerificationPolicySwitchState

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
data class ScanQRState(
    val policy: VerificationPolicyState,
    val lock: VerificationPolicySwitchState,
)
