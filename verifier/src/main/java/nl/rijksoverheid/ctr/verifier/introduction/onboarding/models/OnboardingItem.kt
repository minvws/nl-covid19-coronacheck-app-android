package nl.rijksoverheid.ctr.verifier.introduction.onboarding.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@Parcelize
data class OnboardingItem(
    val imageResource: Int,
    val titleResource: Int,
    val descriptionResource: Int
) : Parcelable
