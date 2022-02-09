/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.shared.models

sealed class DisclosurePolicy(open val stringValue: String) {
    object OneG: DisclosurePolicy("1G")
    object ThreeG: DisclosurePolicy("3G")
    object OneAndThreeG : DisclosurePolicy("1G,3G")

    companion object {
        fun fromString(value: String): DisclosurePolicy? = when (value) {
            "1G" -> OneG
            "3G" -> ThreeG
            "1G,3G" -> OneAndThreeG
            else -> null
        }
    }
}