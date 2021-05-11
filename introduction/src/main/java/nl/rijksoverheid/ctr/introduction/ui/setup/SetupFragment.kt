package nl.rijksoverheid.ctr.introduction.ui.setup

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.appconfig.AppConfigViewModel
import nl.rijksoverheid.ctr.appconfig.AppStatusFragment
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.introduction.R
import nl.rijksoverheid.ctr.shared.utils.Accessibility
import org.koin.androidx.viewmodel.ext.android.viewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class SetupFragment : Fragment(R.layout.fragment_setup) {

    private val args: SetupFragmentArgs by navArgs()
    private val appStatusViewModel: AppConfigViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Accessibility.announce(requireContext(), getString(R.string.app_setup_text))

        appStatusViewModel.appStatusLiveData.observe(viewLifecycleOwner, {
            if (it is AppStatus.NoActionRequired) {
                findNavController().navigate(SetupFragmentDirections.actionOnboarding(args.introductionData))
            } else {
                val bundle = bundleOf(AppStatusFragment.EXTRA_APP_STATUS to it)
                findNavController().navigate(R.id.action_app_status, bundle)
            }
        })

        appStatusViewModel.refresh()
    }
}
