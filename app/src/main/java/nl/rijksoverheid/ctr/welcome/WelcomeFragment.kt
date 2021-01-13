/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.welcome

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.BaseFragment
import nl.rijksoverheid.ctr.R
import nl.rijksoverheid.ctr.databinding.FragmentWelcomeBinding


class WelcomeFragment : BaseFragment(R.layout.fragment_welcome) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentWelcomeBinding.bind(view)


        // Open Coronatest.nl's login flow in the default browser
        binding.btnGetResults.setOnClickListener {
            startActivity(Intent("android.intent.action.VIEW", Uri.parse(getString(R.string.coronatest_result_url))))
        }

        // Open About page using either icon or text
        val aboutThisAppClickListener : View.OnClickListener = View.OnClickListener {view ->
            findNavController().navigate(WelcomeFragmentDirections.toAboutFragment())
        }
        binding.icAboutApp.setOnClickListener(aboutThisAppClickListener)
        binding.textAboutApp.setOnClickListener(aboutThisAppClickListener)
    }
}