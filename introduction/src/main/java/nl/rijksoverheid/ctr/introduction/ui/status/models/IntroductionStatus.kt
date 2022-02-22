package nl.rijksoverheid.ctr.introduction.ui.status.models

import android.os.Parcelable
import androidx.annotation.StringRes
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
    sealed class IntroductionFinished : IntroductionStatus() {
        @Parcelize
        data class ConsentNeeded(val introductionData: IntroductionData) : IntroductionFinished(), Parcelable

        @Parcelize
        object NoActionRequired : IntroductionFinished(), Parcelable

        @Parcelize
        data class NewFeatures(val introductionData: IntroductionData) : IntroductionFinished(), Parcelable

        @Parcelize
        data class NewPolicy(
            @StringRes val title: Int,
            @StringRes val body: Int
        ) : IntroductionFinished(), Parcelable
    }

    @Parcelize
    data class IntroductionNotFinished(val introductionData: IntroductionData) :
        IntroductionStatus(), Parcelable
}
