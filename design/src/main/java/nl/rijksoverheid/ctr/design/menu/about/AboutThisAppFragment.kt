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

    companion object {
        private const val EXTRA_ABOUT_THIS_APP_DATA = "EXTRA_ABOUT_THIS_APP_DATA"

        fun getBundle(data: AboutThisAppData): Bundle {
            val bundle = Bundle()
            bundle.putParcelable(EXTRA_ABOUT_THIS_APP_DATA, data)
            return bundle
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAboutAppBinding.bind(view)

        val aboutThisAppData = arguments?.getParcelable<AboutThisAppData>(EXTRA_ABOUT_THIS_APP_DATA)
            ?: throw IllegalStateException("AboutThisAppData should be set")

        binding.appVersion.text = getString(
            R.string.app_version,
            aboutThisAppData.versionName,
            aboutThisAppData.versionCode
        )
    }
}
