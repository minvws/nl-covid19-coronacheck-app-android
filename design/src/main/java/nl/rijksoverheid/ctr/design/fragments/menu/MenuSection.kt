/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.design.fragments.menu

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize
import nl.rijksoverheid.ctr.design.menu.about.AboutThisAppData

@Parcelize
data class MenuSection(
    val menuItems: List<MenuItem>,
): Parcelable {

    @Parcelize
    data class MenuItem(
        @DrawableRes val icon: Int,
        @StringRes val title: Int,
        val onClick: OnClick
    ): Parcelable {

        sealed class OnClick: Parcelable {
            @Parcelize
            data class OpenBrowser(val url: String): OnClick(), Parcelable

            @Parcelize
            data class Navigate(@IdRes val navigationActionId: Int,
                                val navigationArguments: Bundle? = null): OnClick(), Parcelable
        }
    }
}