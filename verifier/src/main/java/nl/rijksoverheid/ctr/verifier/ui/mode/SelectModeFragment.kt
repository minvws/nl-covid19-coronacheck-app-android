package nl.rijksoverheid.ctr.verifier.ui.mode

import android.os.Bundle
import android.view.View
import android.view.View.VISIBLE
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.FragmentModeSelectBinding
import nl.rijksoverheid.ctr.verifier.ui.scanner.utils.ScannerUtil
import org.koin.android.ext.android.inject


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class SelectModeFragment: Fragment(R.layout.fragment_mode_select) {

    private val scannerUtil: ScannerUtil by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentModeSelectBinding.bind(view)

        if (arguments?.getBoolean(addToolbarArgument) == true) {
            binding.toolbar.visibility = VISIBLE
            binding.toolbar.title = "Voordat je begint"
            binding.toolbar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
            binding.startScanningButton.visibility = VISIBLE
            binding.startScanningButton.setOnClickListener {
                scannerUtil.launchScanner(requireActivity())
            }
        }

    }

    companion object {
        const val addToolbarArgument = "ADD_TOOLBAR_ARGUMENT"
    }
}