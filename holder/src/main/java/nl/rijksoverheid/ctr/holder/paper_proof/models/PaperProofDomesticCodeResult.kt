/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.paper_proof.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class PaperProofDomesticCodeResult : Parcelable {
    @Parcelize
    object Valid : PaperProofDomesticCodeResult(), Parcelable

    @Parcelize
    object Invalid : PaperProofDomesticCodeResult(), Parcelable

    @Parcelize
    object Empty : PaperProofDomesticCodeResult(), Parcelable
}
