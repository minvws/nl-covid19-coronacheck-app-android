package nl.rijksoverheid.ctr.verifier.ui.policy

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
sealed class VerificationPolicyFlow(val state: VerificationPolicyState): Parcelable {
    @Parcelize
    class FirstTimeUse(val policyState: VerificationPolicyState) : VerificationPolicyFlow(policyState)
    @Parcelize
    class Info(val policyState: VerificationPolicyState) : VerificationPolicyFlow(policyState)
}
