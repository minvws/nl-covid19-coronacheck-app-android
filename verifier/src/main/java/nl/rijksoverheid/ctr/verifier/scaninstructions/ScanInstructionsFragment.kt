package nl.rijksoverheid.ctr.verifier.scaninstructions

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.FragmentScanInstructionsBinding

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ScanInstructionsFragment : Fragment(R.layout.fragment_scan_instructions) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentScanInstructionsBinding.bind(view)
        GroupAdapter<GroupieViewHolder>()
            .run {
                addAll(
                    listOf(
                        ScanInstructionAdapterItem(
                            title = R.string.scan_instructions_1_title,
                            description = R.string.scan_instructions_1_description,
                        ),
                        ScanInstructionAdapterItem(
                            title = R.string.scan_instructions_2_title,
                            description = R.string.scan_instructions_2_description,
                            image = R.drawable.illustration_scan_instruction_2
                        ),
                        ScanInstructionAdapterItem(
                            title = R.string.scan_instructions_3_title,
                            description = R.string.scan_instructions_3_description,
                            image = R.drawable.illustration_scan_instruction_3
                        ),
                        ScanInstructionAdapterItem(
                            title = R.string.scan_instructions_4_title,
                            description = R.string.scan_instructions_4_description
                        )
                    )
                )
                binding.recyclerView.adapter = this
            }
    }
}
