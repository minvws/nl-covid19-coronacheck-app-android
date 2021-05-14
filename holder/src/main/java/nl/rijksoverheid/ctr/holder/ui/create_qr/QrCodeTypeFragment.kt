package nl.rijksoverheid.ctr.holder.ui.create_qr

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentQrCodeTypeBinding

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class QrCodeTypeFragment : Fragment(R.layout.fragment_qr_code_type) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentQrCodeTypeBinding.bind(view)

        binding.negativeTestButton.bind(
            R.string.qr_code_type_negative_test_title,
            getString(R.string.qr_code_type_negative_test_description)
        ) {
            findNavController().navigate(QrCodeTypeFragmentDirections.actionChooseProvider())
        }

        binding.vaccinationButton.bind(
            R.string.qr_code_type_vaccination_title,
            getString(R.string.qr_code_type_vaccination_description)
        ) {
            findNavController().navigate(QrCodeTypeFragmentDirections.actionVaccination())
        }
    }
}
