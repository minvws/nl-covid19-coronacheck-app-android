/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.design.menu.about

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.design.R
import nl.rijksoverheid.ctr.design.databinding.FragmentAboutAppBinding

class AboutThisAppFragment : Fragment(R.layout.fragment_about_app) {

    private val aboutAppData by lazy { (requireActivity().application as AboutAppResourceProvider).getAboutThisAppData() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAboutAppBinding.bind(view)

        binding.description.text = getString(aboutAppData.aboutThisAppTextResource)
        binding.appVersion.text = getString(
            aboutAppData.appVersionTextResource,
            aboutAppData.appVersionName,
            aboutAppData.appVersionCode
        )

    }
}