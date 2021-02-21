package nl.rijksoverheid.ctr.holder.status

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.holder.HideToolbar
import nl.rijksoverheid.ctr.holder.introduction.IntroductionViewModel
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

        introductionViewModel.introductionStateLiveData.observe(this, { state ->
            state.onboardingFinished
            val direction = when {
                !state.onboardingFinished -> StatusFragmentDirections.actionOnboarding()
                !state.privacyPolicyFinished -> StatusFragmentDirections.actionPrivacyPolicy()
                else -> StatusFragmentDirections.actionHome()
            }
            findNavController().navigate(direction)
        })
    }
}
