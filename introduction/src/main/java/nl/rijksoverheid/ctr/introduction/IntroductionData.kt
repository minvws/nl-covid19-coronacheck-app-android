package nl.rijksoverheid.ctr.introduction

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import nl.rijksoverheid.ctr.introduction.ui.new_features.models.NewFeatureItem
import nl.rijksoverheid.ctr.introduction.ui.new_terms.models.NewTerms
import nl.rijksoverheid.ctr.introduction.ui.onboarding.models.OnboardingItem
import nl.rijksoverheid.ctr.introduction.ui.privacy_consent.models.PrivacyPolicyItem

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
    val newFeatures: List<NewFeatureItem> = listOf(),
    val newTerms: NewTerms? = null,
    val newFeatureVersion: Int = 0,
    val hideConsent: Boolean = false
) : Parcelable
