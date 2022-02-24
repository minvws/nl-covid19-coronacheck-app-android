/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.shared.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class DisclosurePolicy(open val stringValue: String): Parcelable {
    @Parcelize object OneG: DisclosurePolicy("1G")
    @Parcelize object ThreeG: DisclosurePolicy("3G")
    @Parcelize object OneAndThreeG : DisclosurePolicy("1G,3G")

    companion object {
        fun fromString(value: String): DisclosurePolicy? = when (value) {
            "1G" -> OneG
            "3G" -> ThreeG
            "1G,3G" -> OneAndThreeG
            else -> null
        }
    }
}