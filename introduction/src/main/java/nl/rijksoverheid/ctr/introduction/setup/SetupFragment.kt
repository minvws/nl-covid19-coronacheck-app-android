package nl.rijksoverheid.ctr.introduction.setup

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.introduction.CoronaCheckApp
import nl.rijksoverheid.ctr.introduction.R
import nl.rijksoverheid.ctr.introduction.databinding.FragmentSetupBinding

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class SetupFragment : Fragment(R.layout.fragment_setup) {

    private val introductionData by lazy { (requireActivity().application as CoronaCheckApp).getIntroductionData() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentSetupBinding.bind(view)
        binding.root.setBackgroundResource(introductionData.launchScreen)
        binding.text.setText(introductionData.appSetupTextResource)

        Handler(Looper.getMainLooper()).postDelayed({
            findNavController().navigate(SetupFragmentDirections.actionOnboarding())
        }, 2000)
    }

}
