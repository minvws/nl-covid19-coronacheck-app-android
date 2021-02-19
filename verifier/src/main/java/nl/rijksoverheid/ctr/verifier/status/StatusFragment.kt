package nl.rijksoverheid.ctr.verifier.status

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.verifier.HideToolbar
import nl.rijksoverheid.ctr.verifier.introduction.IntroductionViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class StatusFragment : Fragment(), HideToolbar {
    private val introductionViewModel: IntroductionViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        introductionViewModel.introductionStateLiveData.observe(this, Observer { state ->
            state.onboardingFinished
            val direction = when {
                !state.onboardingFinished -> StatusFragmentDirections.actionOnboarding()
                !state.privacyPolicyFinished -> StatusFragmentDirections.actionPrivacyPolicy()
                else -> StatusFragmentDirections.actionScanQr()
            }
            findNavController().navigate(direction)
        })
    }
}