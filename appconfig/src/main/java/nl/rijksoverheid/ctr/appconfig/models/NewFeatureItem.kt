package nl.rijksoverheid.ctr.appconfig.models

import android.os.Parcelable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize
import nl.rijksoverheid.ctr.appconfig.R

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
    @StringRes val description: Int,
    @ColorRes val subTitleColor: Int? = null,
    @ColorRes val backgroundColor: Int? = null,
    @StringRes val subtitleResource: Int = R.string.new_in_app_subtitle,
    @StringRes val buttonResource: Int = R.string.onboarding_next,
    @StringRes val lastButtonResource: Int = R.string.general_toMyOverview
) : Parcelable
