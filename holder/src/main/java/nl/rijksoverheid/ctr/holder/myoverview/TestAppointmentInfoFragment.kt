package nl.rijksoverheid.ctr.holder.myoverview

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentTestAppointmentInfoBinding
import nl.rijksoverheid.ctr.shared.ext.fromHtml

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class TestAppointmentInfoFragment : Fragment(R.layout.fragment_test_appointment_info) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentTestAppointmentInfoBinding.bind(view)
        binding.description.text =
            getString(R.string.test_appointment_info_description).fromHtml()
        binding.button.setOnClickListener {
            findNavController().navigate(TestAppointmentInfoFragmentDirections.actionChooseProvider())
        }
    }

}
