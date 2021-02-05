package nl.rijksoverheid.ctr.holder.status

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.holder.HideToolbar
import nl.rijksoverheid.ctr.shared.ext.observeResult
import nl.rijksoverheid.ctr.shared.models.AppStatus
import nl.rijksoverheid.ctr.shared.models.AppStatus.AppDeactivated
import nl.rijksoverheid.ctr.shared.models.AppStatus.ShouldUpdate
import org.koin.android.viewmodel.ext.android.viewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class StatusFragment : Fragment(), HideToolbar {

    private val statusViewModel: StatusViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        observeResult(statusViewModel.appStatusLiveData, {
            // TODO: Implement loading state
        }, {
            when (it) {
                is AppDeactivated -> {
                    // TODO: Implement app deactivated UI
                }
                is ShouldUpdate -> {
                    // TODO: Implement should update UI
                }
                is AppStatus.Ok -> {
                    statusViewModel.getOnboardingFinished()
                }
            }
        }, {
            val i = 5
            // TODO: Implement error UI
        })

        statusViewModel.onboardingFinishedLiveData.observe(this, Observer { finished ->
            val directions =
                if (finished) StatusFragmentDirections.actionMyqr() else StatusFragmentDirections.actionOnboarding()
            findNavController().navigate(directions)
        })

        statusViewModel.getAppStatus()
    }
}
