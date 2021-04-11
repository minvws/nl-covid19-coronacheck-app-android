package nl.rijksoverheid.ctr.verifier.ui.scanqr

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.shared.ext.fromHtml
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.FragmentScanQrBinding

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ScanQrFragment : Fragment(R.layout.fragment_scan_qr) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentScanQrBinding.bind(view)
        binding.description.text = binding.description.text.toString().fromHtml()
        binding.description.setOnClickListener {
            findNavController().navigate(ScanQrFragmentDirections.actionScanInstructions())
        }

        binding.button.setOnClickListener {
            Navigation.findNavController(requireActivity(), R.id.main_nav_host_fragment)
                .navigate(R.id.nav_scanner)
        }
    }
}


