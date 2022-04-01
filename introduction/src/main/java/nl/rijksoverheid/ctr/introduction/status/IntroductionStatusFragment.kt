package nl.rijksoverheid.ctr.introduction.status

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.introduction.status.models.IntroductionStatus
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class IntroductionStatusFragment : Fragment() {

    private val args: IntroductionStatusFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (val status = args.introductionStatus) {
            is IntroductionStatus.SetupNotFinished,
            is IntroductionStatus.OnboardingNotFinished -> {
                findNavControllerSafety()?.navigate(
                    IntroductionStatusFragmentDirections.actionSetup()
                )
            }
            IntroductionStatus.IntroductionFinished -> { }
        }
    }
}
