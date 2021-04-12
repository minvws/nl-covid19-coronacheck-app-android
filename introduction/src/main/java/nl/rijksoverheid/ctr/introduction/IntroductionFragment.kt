package nl.rijksoverheid.ctr.introduction

import android.os.Bundle
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.introduction.models.IntroductionData
import nl.rijksoverheid.ctr.introduction.models.IntroductionStatus

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class IntroductionFragment : Fragment(R.layout.fragment_introduction) {

    companion object {
        private const val EXTRA_INTRODUCTION_DATA = "EXTRA_INTRODUCTION_DATA"
        private const val EXTRA_INTRODUCTION_STATUS = "EXTRA_INTRODUCTION_STATUS"

        fun getBundle(
            introductionData: IntroductionData,
            introductionStatus: IntroductionStatus
        ): Bundle {
            val bundle = Bundle()
            bundle.putParcelable(EXTRA_INTRODUCTION_DATA, introductionData)
            bundle.putParcelable(EXTRA_INTRODUCTION_STATUS, introductionStatus)
            return bundle
        }
    }

    val introductionData by lazy {
        arguments?.getParcelable<IntroductionData>(
            EXTRA_INTRODUCTION_DATA
        ) ?: error("IntroductionData should be set")
    }

    val introductionStatus by lazy {
        arguments?.getParcelable<IntroductionStatus>(
            EXTRA_INTRODUCTION_STATUS
        )
    }
}
