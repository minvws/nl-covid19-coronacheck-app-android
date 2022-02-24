package nl.rijksoverheid.ctr.introduction.ui.status.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import nl.rijksoverheid.ctr.introduction.IntroductionData

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
sealed class IntroductionStatus : Parcelable {

    @Parcelize
    object SetupNotFinished : IntroductionStatus(), Parcelable

    @Parcelize
    data class OnboardingNotFinished(val introductionData: IntroductionData) :
        IntroductionStatus(), Parcelable

    sealed class OnboardingFinished : IntroductionStatus() {

        @Parcelize
        data class ConsentNeeded(val introductionData: IntroductionData) : OnboardingFinished(), Parcelable

        @Parcelize
        data class NewFeatures(val introductionData: IntroductionData) : OnboardingFinished(), Parcelable
    }

    @Parcelize
    object IntroductionFinished : IntroductionStatus(), Parcelable
}
