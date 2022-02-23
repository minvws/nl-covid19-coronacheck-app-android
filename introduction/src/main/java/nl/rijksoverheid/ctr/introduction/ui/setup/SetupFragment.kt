package nl.rijksoverheid.ctr.introduction.ui.setup

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.appconfig.AppConfigViewModel
import nl.rijksoverheid.ctr.appconfig.AppStatusFragment
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.design.utils.IntentUtil
import nl.rijksoverheid.ctr.introduction.IntroductionData
import nl.rijksoverheid.ctr.introduction.IntroductionViewModel
import nl.rijksoverheid.ctr.introduction.R
import nl.rijksoverheid.ctr.introduction.ui.status.models.IntroductionStatus
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import nl.rijksoverheid.ctr.shared.utils.Accessibility
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class SetupFragment : Fragment(R.layout.fragment_setup) {

    private val appStatusViewModel: AppConfigViewModel by sharedViewModel()
    private val introductionViewModel: IntroductionViewModel by viewModel()
    private val mobileCoreWrapper: MobileCoreWrapper by inject()
    private val dialogUtil: DialogUtil by inject()
    private val intentUtil: IntentUtil by inject()

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
            when (it) {
                is AppStatus.UpdateRecommended -> showRecommendedUpdateDialog()
                is AppStatus.NoActionRequired -> introductionViewModel.onConfigUpdated()
                else -> navigateToAppStatus(it)
            }
        }
        introductionViewModel.introductionNotFinishedLiveData.observe(
            viewLifecycleOwner, EventObserver {
                navigateToOnboarding(it.introductionData)
            })
    }

    private fun navigateToAppStatus(appStatus: AppStatus) {
        findNavControllerSafety()?.navigate(
            R.id.action_app_status,
            bundleOf(AppStatusFragment.EXTRA_APP_STATUS to appStatus)
        )
    }

    private fun navigateToOnboarding(introductionData: IntroductionData) {
        findNavControllerSafety()?.navigate(
            SetupFragmentDirections.actionOnboarding(introductionData)
        )
    }

    private fun showRecommendedUpdateDialog() {
        dialogUtil.presentDialog(
            context = requireContext(),
            title = R.string.app_status_update_recommended_title,
            message = getString(R.string.app_status_update_recommended_message),
            positiveButtonText = R.string.app_status_update_recommended_action,
            positiveButtonCallback = { intentUtil.openPlayStore(requireContext()) },
            negativeButtonText = R.string.app_status_update_recommended_dismiss_action,
            onDismissCallback = {
                (introductionViewModel.getIntroductionStatus() as? IntroductionStatus.IntroductionNotFinished)
                    ?.introductionData?.let { navigateToOnboarding(it) }
            }
        )
    }
}
