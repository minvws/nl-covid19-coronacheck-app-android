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
sealed class VerificationPolicySelectionState : Parcelable {

    sealed class Selection : VerificationPolicySelectionState() {

        @Parcelize
        object Policy3G : Selection()

        @Parcelize
        object Policy1G : Selection()

        @Parcelize
        object None : Selection()
    }

    @Parcelize
    object Policy1G: VerificationPolicySelectionState()

    @Parcelize
    object Policy3G: VerificationPolicySelectionState()
}
