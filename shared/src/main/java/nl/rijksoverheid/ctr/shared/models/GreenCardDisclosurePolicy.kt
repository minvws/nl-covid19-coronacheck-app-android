/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.shared.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


sealed class GreenCardDisclosurePolicy: Parcelable {

    @Parcelize
    object OneG: GreenCardDisclosurePolicy(), Parcelable

    @Parcelize
    object ThreeG: GreenCardDisclosurePolicy(), Parcelable
}