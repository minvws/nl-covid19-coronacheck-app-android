package nl.rijksoverheid.ctr.introduction.onboarding.models

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize
import nl.rijksoverheid.ctr.introduction.onboarding.OnboardingBaseItemFragment
import nl.rijksoverheid.ctr.introduction.onboarding.OnboardingItemFragment

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@Parcelize
open class OnboardingItem(
    @DrawableRes val imageResource: Int = 0,
    @StringRes val titleResource: Int,
    @StringRes val description: Int,
    val position: Int = -1, // Holds position in viewpager to show current step in progress
    @RawRes val animationResource: Int = 0,
    val clazz: Class<out OnboardingBaseItemFragment> = OnboardingItemFragment::class.java
) : Parcelable
