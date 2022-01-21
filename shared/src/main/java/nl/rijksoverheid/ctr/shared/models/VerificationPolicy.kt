package nl.rijksoverheid.ctr.shared.models

import mobilecore.Mobilecore

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
sealed class VerificationPolicy(val libraryValue: String) {
    object VerificationPolicy2G : VerificationPolicy(Mobilecore.VERIFICATION_POLICY_2G)
    object VerificationPolicy3G : VerificationPolicy(Mobilecore.VERIFICATION_POLICY_3G)
    // TODO replace "4" and "5" with library static value when ready
    object VerificationPolicy2GPlus : VerificationPolicy("4")
    object VerificationPolicy1G : VerificationPolicy("5")

    companion object {
        fun fromString(value: String?): VerificationPolicy? = when (value) {
            Mobilecore.VERIFICATION_POLICY_2G -> VerificationPolicy2G
            Mobilecore.VERIFICATION_POLICY_3G -> VerificationPolicy3G
            "4" -> VerificationPolicy2GPlus
            else -> null
        }
    }
}
