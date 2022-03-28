package nl.rijksoverheid.ctr.introduction.setup

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.appconfig.AppConfigViewModel
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.introduction.IntroductionViewModel
import nl.rijksoverheid.ctr.introduction.R
import nl.rijksoverheid.ctr.introduction.status.models.IntroductionStatus
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import nl.rijksoverheid.ctr.shared.utils.Accessibility
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class SetupFragment : Fragment(R.layout.fragment_setup) {

    private val appStatusViewModel: AppConfigViewModel by sharedViewModel()
    private val mobileCoreWrapper: MobileCoreWrapper by inject()
    private val introductionViewModel: IntroductionViewModel by sharedViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Accessibility.announce(requireContext(), getString(R.string.app_setup_text))

        setObservers()
    }

    override fun onStart() {
        super.onStart()
        appStatusViewModel.refresh(mobileCoreWrapper, true)
    }

    private fun setObservers() {
        appStatusViewModel.appStatusLiveData.observe(viewLifecycleOwner) {
            if (it is AppStatus.NoActionRequired || it is AppStatus.UpdateRecommended) {
                introductionViewModel.onConfigUpdated()
            }
        }
        introductionViewModel.introductionStatusLiveData.observe(viewLifecycleOwner, EventObserver {
            if (it is IntroductionStatus.OnboardingNotFinished) {
                findNavControllerSafety()?.navigate(
                    SetupFragmentDirections.actionOnboarding(it.introductionData)
                )
            }
        })
    }
}
