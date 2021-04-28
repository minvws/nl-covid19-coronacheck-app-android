package nl.rijksoverheid.ctr.holder.ui.make_appointment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.appconfig.AppConfigUtil
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentMakeAppointmentBinding
import nl.rijksoverheid.ctr.shared.ext.launchUrl
import nl.rijksoverheid.ctr.shared.utils.AndroidUtil
import org.koin.android.ext.android.inject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class MakeAppointmentFragment : Fragment(R.layout.fragment_make_appointment) {

    private val androidUtil: AndroidUtil by inject()
    private val appConfigUtil: AppConfigUtil by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentMakeAppointmentBinding.bind(view)
        binding.description.setHtmlText(appConfigUtil.getStringWithTestValidity(R.string.test_appointment_info_description), false)
        binding.button.setOnClickListener {
            getString(R.string.url_make_appointment).launchUrl(requireContext())
        }

        if (androidUtil.isSmallScreen()) {
            binding.image.visibility = View.GONE
        }
    }
}
