package nl.rijksoverheid.ctr.introduction.setup

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.appconfig.AppConfigViewModel
import nl.rijksoverheid.ctr.appconfig.AppStatusFragment
import nl.rijksoverheid.ctr.appconfig.model.AppStatus
import nl.rijksoverheid.ctr.introduction.CoronaCheckApp
import nl.rijksoverheid.ctr.introduction.R
import nl.rijksoverheid.ctr.introduction.databinding.FragmentSetupBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class SetupFragment : Fragment(R.layout.fragment_setup) {

    private val appStatusViewModel: AppConfigViewModel by viewModel()
    private val introductionData by lazy { (requireActivity().application as CoronaCheckApp).getIntroductionData() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentSetupBinding.bind(view)
        binding.root.setBackgroundResource(introductionData.launchScreen)
        binding.text.setText(introductionData.appSetupTextResource)

        appStatusViewModel.appStatusLiveData.observe(viewLifecycleOwner, {
            if (it is AppStatus.NoActionRequired) {
                findNavController().navigate(SetupFragmentDirections.actionOnboarding())
            } else {
                val bundle = bundleOf(AppStatusFragment.EXTRA_APP_STATUS to it)
                findNavController().navigate(R.id.action_app_status, bundle)
            }
        })

        appStatusViewModel.refresh()

    }

}
