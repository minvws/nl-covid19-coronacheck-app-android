/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.paper_proof.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class PaperProofCodeResult: Parcelable {
    @Parcelize
    object Valid: PaperProofCodeResult(), Parcelable

    @Parcelize
    object Invalid: PaperProofCodeResult(), Parcelable

    @Parcelize
    object Empty: PaperProofCodeResult(), Parcelable
}