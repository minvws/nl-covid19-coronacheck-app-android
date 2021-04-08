package nl.rijksoverheid.ctr.introduction.models

import android.os.Parcelable
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize
import nl.rijksoverheid.ctr.introduction.onboarding.models.OnboardingItem
import nl.rijksoverheid.ctr.introduction.privacy_consent.models.PrivacyPolicyItem

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@Parcelize
data class IntroductionData(
    val onboardingItems: List<OnboardingItem> = listOf(),
    val privacyPolicyItems: List<PrivacyPolicyItem> = listOf(),
    @StringRes val appSetupTextResource: Int,
    @StringRes val privacyPolicyStringResource: Int,
    @StringRes val privacyPolicyCheckboxStringResource: Int,
    @StringRes val onboardingNextButtonStringResource: Int,
    @StringRes val backButtonStringResource: Int,
    @StringRes val onboardingPageIndicatorStringResource: Int
) : Parcelable
