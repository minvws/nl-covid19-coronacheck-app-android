package nl.rijksoverheid.ctr.verifier.ui.scanqr.scaninstructions

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.FragmentScanInstructionsBinding
import nl.rijksoverheid.ctr.verifier.ui.scanner.utils.ScannerUtil
import org.koin.android.ext.android.inject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ScanInstructionsFragment : Fragment(R.layout.fragment_scan_instructions) {

    private val scannerUtil: ScannerUtil by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentScanInstructionsBinding.bind(view)
        binding.button.setOnClickListener {
            scannerUtil.launchScanner(requireActivity())
        }

        GroupAdapter<GroupieViewHolder>()
            .run {
                addAll(
                    listOf(
                        ScanInstructionAdapterItem(
                            title = R.string.scan_instructions_1_title,
                            description = getString(R.string.scan_instructions_1_description),
                        ),
                        ScanInstructionAdapterItem(
                            title = R.string.scan_instructions_2_title,
                            description = getString(R.string.scan_instructions_2_description),
                        ),
                        ScanInstructionAdapterItem(
                            image = R.drawable.illustration_scan_instruction_3,
                            imageDescription = getString(R.string.scan_instructions_3_image),
                            title = R.string.scan_instructions_3_title,
                            description = getString(R.string.scan_instructions_3_description),
                        ),
                        ScanInstructionAdapterItem(
                            image = R.drawable.illustration_scan_instruction_4,
                            imageDescription = getString(R.string.scan_instructions_4_image),
                            title = R.string.scan_instructions_4_title,
                            description = getString(R.string.scan_instructions_4_description)
                        )
                    )
                )
                binding.recyclerView.adapter = this
            }
    }
}
