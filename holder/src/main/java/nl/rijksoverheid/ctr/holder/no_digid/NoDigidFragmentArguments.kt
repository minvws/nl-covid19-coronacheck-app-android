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
import nl.rijksoverheid.ctr.design.fragments.info.InfoFragmentData
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteOriginType

@Parcelize
data class NoDigidFragmentData(
    val title: String,
    val description: String,
    val firstNavigationButtonData: NoDigidNavigationButtonData,
    val secondNavigationButtonData: NoDigidNavigationButtonData,
    val originType: RemoteOriginType
) : Parcelable

sealed class NoDigidNavigationButtonData(
    @StringRes open val title: Int,
    open val subtitle: String? = null,
    @DrawableRes open val icon: Int? = null
) : Parcelable {

    @Parcelize
    data class NoDigid(
        @StringRes override val title: Int,
        override val subtitle: String? = null,
        @DrawableRes override val icon: Int? = null,
        val noDigidFragmentData: NoDigidFragmentData
    ) : NoDigidNavigationButtonData(title, subtitle, icon)

    @Parcelize
    data class Info(
        @StringRes override val title: Int,
        override val subtitle: String? = null,
        @DrawableRes override val icon: Int? = null,
        val infoFragmentData: InfoFragmentData.TitleDescriptionWithButton
    ) : NoDigidNavigationButtonData(title, subtitle, icon)

    @Parcelize
    data class Link(
        @StringRes override val title: Int,
        override val subtitle: String? = null,
        @DrawableRes override val icon: Int? = null,
        val externalUrl: String
    ) : NoDigidNavigationButtonData(title, subtitle, icon)

    @Parcelize
    data class Ggd(
        @StringRes override val title: Int,
        override val subtitle: String? = null,
        @DrawableRes override val icon: Int? = null
    ) : NoDigidNavigationButtonData(title, subtitle, icon)
}
