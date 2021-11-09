package nl.rijksoverheid.ctr.verifier.ui.mode

import android.os.Bundle
import android.view.View
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.shared.ext.children
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.FragmentModeSelectBinding
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
import nl.rijksoverheid.ctr.verifier.ui.scanner.utils.ScannerUtil
import nl.rijksoverheid.ctr.verifier.ui.scanqr.ScanQrViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class SelectModeFragment: Fragment(R.layout.fragment_mode_select) {

    private val scannerUtil: ScannerUtil by inject()
    private val persistenceManager: PersistenceManager by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentModeSelectBinding.bind(view)

        if (arguments?.getBoolean(addToolbarArgument) == true) {
            binding.toolbar.visibility = VISIBLE
            setTitleTwoLines(binding.toolbar)

            binding.toolbar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
            binding.startScanningButton.visibility = VISIBLE
            binding.startScanningButton.setOnClickListener {
                scannerUtil.launchScanner(requireActivity())
//                persistenceManager.setHighRiskModeSelected()
            }
        }

    }

    private fun setTitleTwoLines(toolbar: Toolbar) {
        val titleTextView = toolbar.children.firstOrNull() {
            it is AppCompatTextView && it.text == getString(R.string.risk_mode_selection_title)
        } as? AppCompatTextView
        titleTextView?.isSingleLine = false
    }

    companion object {
        const val addToolbarArgument = "ADD_TOOLBAR_ARGUMENT"
    }
}