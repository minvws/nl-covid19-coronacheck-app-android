/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.no_digid

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize

@Parcelize
data class NoDigidFragmentData(
    val title: String,
    val description: String,
    val firstNavigationButtonData: NavigationButtonData,
    val secondNavigationButtonData: NavigationButtonData
) : Parcelable

@Parcelize
data class ButtonClickDirection(@IdRes val actionId: Int, val arguments: Bundle) : Parcelable

@Parcelize
data class NavigationButtonData(
    @StringRes val title: Int,
    val subtitle: String? = null,
    @DrawableRes val icon: Int? = null,
    val buttonClickDirection: ButtonClickDirection? = null,
    val externalUrl: String? = null
) : Parcelable

