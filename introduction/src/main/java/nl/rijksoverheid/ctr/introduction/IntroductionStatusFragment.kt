package nl.rijksoverheid.ctr.introduction

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.introduction.models.IntroductionStatus

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class IntroductionStatusFragment : Fragment() {

    private val introductionStatus by lazy { (parentFragment?.parentFragment as IntroductionFragment).introductionStatus }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (val status = introductionStatus) {
            is IntroductionStatus.IntroductionNotFinished -> {
                findNavController().navigate(IntroductionStatusFragmentDirections.actionSetup())
            }
            is IntroductionStatus.IntroductionFinished.ConsentNeeded -> {
                findNavController().navigate(
                    IntroductionStatusFragmentDirections.actionNewTerms(
                        status.newTerms
                    )
                )
            }
        }
    }
}
