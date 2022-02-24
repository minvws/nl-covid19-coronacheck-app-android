package nl.rijksoverheid.ctr.shared.models

import mobilecore.Mobilecore

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
sealed class VerificationPolicy(val libraryValue: String, val configValue: String) {
    object VerificationPolicy3G : VerificationPolicy(Mobilecore.VERIFICATION_POLICY_3G, "3G")
    object VerificationPolicy1G : VerificationPolicy(Mobilecore.VERIFICATION_POLICY_1G, "1G")

    companion object {
        fun fromString(value: String?): VerificationPolicy? = when (value) {
            Mobilecore.VERIFICATION_POLICY_3G -> VerificationPolicy3G
            Mobilecore.VERIFICATION_POLICY_1G -> VerificationPolicy1G
            else -> null
        }

        fun fromConfigValue(value: String): VerificationPolicy? = when (value) {
            VerificationPolicy3G.configValue -> VerificationPolicy3G
            VerificationPolicy1G.configValue -> VerificationPolicy1G
            else -> null
        }
    }
}
