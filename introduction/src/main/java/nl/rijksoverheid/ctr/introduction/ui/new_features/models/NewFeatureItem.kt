package nl.rijksoverheid.ctr.introduction.ui.new_features.models

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@Parcelize
data class NewFeatureItem(
    @DrawableRes val imageResource: Int,
    @StringRes val titleResource: Int,
    @StringRes val description: Int
) : Parcelable