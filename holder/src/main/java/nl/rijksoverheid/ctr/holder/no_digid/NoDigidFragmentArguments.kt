/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.no_digid

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize

@Parcelize
data class NoDigidFragmentData(val title: String, val description: String, val firstButtonData: ButtonData, val secondButtonData: ButtonData): Parcelable

@Parcelize
data class ButtonData(@StringRes val title: Int,val subtitle: String? = null, @DrawableRes val icon: Int? = null): Parcelable